package ir.ac.kntu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ir.ac.kntu.entities.enums.ReservationStatus;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.PhysicalItem;
import ir.ac.kntu.entities.module.Reservation;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.ReservationRepo;
import ir.ac.kntu.entities.enums.NotificationType;
import ir.ac.kntu.util.ServiceResult;

public class ReservationService {
    private final ReservationRepo reservationRepo;
    private final ItemRepo itemRepo;
    private NotificationService notificationService;

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ReservationService(ReservationRepo reservationRepo, ItemRepo itemRepo) {
        this.reservationRepo = reservationRepo;
        this.itemRepo = itemRepo;
    }

    public ServiceResult<Reservation> reserveItem(User user, String itemId) {
        if (!(user instanceof Client)) {
            return ServiceResult.failure("Only clients can reserve items.");
        }
        Client client = (Client) user;

        Item item = itemRepo.findById(itemId);
        if (item == null) {
            return ServiceResult.failure("Item not found.");
        }
        if (!(item instanceof PhysicalItem)) {
            return ServiceResult.failure("Only physical items can be reserved.");
        }

        List<Reservation> userActive = reservationRepo.findByUserId(user.getId())
                .stream()
                .filter(r -> r.getItemId().equals(itemId) && r.isActive())
                .collect(Collectors.toList());
        if (!userActive.isEmpty()) {
            return ServiceResult.failure("You already have an active reservation for this item.");
        }

        long activeCount = reservationRepo.findByUserId(user.getId()).stream()
                .filter(Reservation::isActive)
                .count();
        LibraryPolicy policy = LibraryPolicy.getInstance();
        int maxRes = policy.getMaxReservations(client.getRole());
        if (activeCount >= maxRes) {
            return ServiceResult.failure("Reservation limit reached (" + maxRes + ").");
        }

        Reservation reservation = new Reservation(user.getId(), itemId);
        reservationRepo.save(reservation);
        return ServiceResult.success(reservation,
                "Reserved successfully. You will be notified when a copy is available.");
    }

    public ServiceResult<Void> cancelReservation(User user, String reservationId) {
        Reservation res = reservationRepo.findById(reservationId);
        if (res == null) {
            return ServiceResult.failure("Reservation not found.");
        }
        if (!res.getUserId().equals(user.getId())) {
            return ServiceResult.failure("This is not your reservation.");
        }
        if (!res.isActive()) {
            return ServiceResult.failure("Reservation is no longer active.");
        }

        Item item = itemRepo.findById(res.getItemId());

        if (res.getStatus() == ReservationStatus.NOTIFIED && item instanceof PhysicalItem phys) {
            phys.setAvailableCopies(phys.getAvailableCopies() + 1);
            itemRepo.save(phys);
            processNextInQueue(phys);
        }

        res.cancel();
        reservationRepo.save(res);
        return ServiceResult.success("Reservation cancelled.");
    }

    public void handleItemReturned(PhysicalItem item) {
        processExpiredReservations(item);
        processNextInQueue(item);
    }

    public void processExpiredReservations(PhysicalItem item) {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> allForItem = reservationRepo.findByItemIdSorted(item.getId());

        List<Reservation> toExpire = allForItem.stream()
                .filter(r -> r.getStatus() == ReservationStatus.NOTIFIED)
                .filter(r -> r.getClaimDeadline() != null)
                .filter(r -> r.getClaimDeadline().isBefore(now))
                .toList();

        for (Reservation r : toExpire) {
            r.expire();
            reservationRepo.save(r);
            item.setAvailableCopies(item.getAvailableCopies() + 1);
        }

        if (!toExpire.isEmpty()) {
            itemRepo.save(item);
            processNextInQueue(item);
        }
    }

    public Reservation getActiveReservationForUserAndItem(String userId, String itemId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepo.findByUserId(userId).stream()
                .filter(r -> r.getItemId().equals(itemId)
                        && r.getStatus() == ReservationStatus.NOTIFIED
                        && r.getClaimDeadline() != null
                        && r.getClaimDeadline().isAfter(now))
                .findFirst().orElse(null);
    }

    public void fulfillReservation(String reservationId) {
        Reservation res = reservationRepo.findById(reservationId);
        if (res != null && res.isActive()) {
            res.fulfill();
            reservationRepo.save(res);
        }
    }

    public List<Reservation> getUserActiveReservations(User user) {
        return reservationRepo.findByUserId(user.getId()).stream()
                .filter(Reservation::isActive)
                .collect(Collectors.toList());
    }

    public List<Reservation> getUserReservations(User user) {
        return reservationRepo.findByUserId(user.getId()).stream()
                .sorted((a, b) -> b.getReservedAt().compareTo(a.getReservedAt()))
                .collect(Collectors.toList());
    }

    public ServiceResult<Void> adminCancelReservation(String reservationId) {
        Reservation res = reservationRepo.findById(reservationId);
        if (res == null) {
            return ServiceResult.failure("Reservation not found.");
        }
        if (!res.isActive()) {
            return ServiceResult.failure("Reservation is no longer active.");
        }

        Item item = itemRepo.findById(res.getItemId());
        if (res.getStatus() == ReservationStatus.NOTIFIED && item instanceof PhysicalItem phys) {
            phys.setAvailableCopies(phys.getAvailableCopies() + 1);
            itemRepo.save(phys);
            processNextInQueue(phys);
        }

        res.cancel();
        reservationRepo.save(res);
        return ServiceResult.success("Reservation cancelled by admin.");
    }

    private void processNextInQueue(PhysicalItem item) {
        if (item.getAvailableCopies() <= 0) {
            itemRepo.save(item);
            return;
        }

        Reservation next = reservationRepo.findFirstWaiting(item.getId());
        if (next != null) {
            int pickupDays = LibraryPolicy.getInstance().getReservationPickupDays();
            next.notifyUser(pickupDays);
            reservationRepo.save(next);
            item.setAvailableCopies(item.getAvailableCopies() - 1);
            itemRepo.save(item);
            if (notificationService != null) {
                String tag = "res-available-" + next.getReservationId();
                String msg = "\"" + item.getTitle() + "\" is ready for pickup! Collect before "
                        + next.getClaimDeadline().toLocalDate() + ".";
                notificationService.pushTagged(next.getUserId(), "Reservation Ready",
                        msg, NotificationType.INFO, tag);
            }
        } else {
            itemRepo.save(item);
        }
    }
}