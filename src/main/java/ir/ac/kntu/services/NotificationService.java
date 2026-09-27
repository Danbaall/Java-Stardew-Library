package ir.ac.kntu.services;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.entities.enums.NotificationType;
import ir.ac.kntu.entities.enums.ReservationStatus;
import ir.ac.kntu.entities.module.Notification;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.NotificationRepo;
import ir.ac.kntu.repo.ReservationRepo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {

    private final NotificationRepo repo;
    private final BorrowHistoryRepo borrowHistoryRepo;
    private final ReservationRepo reservationRepo;
    private final ItemRepo itemRepo;

    private final ObservableList<Notification> currentUserNotifications = FXCollections.observableArrayList();
    private String currentUserId;

    public NotificationService(NotificationRepo repo,
            BorrowHistoryRepo borrowHistoryRepo,
            ReservationRepo reservationRepo,
            ItemRepo itemRepo) {
        this.repo = repo;
        this.borrowHistoryRepo = borrowHistoryRepo;
        this.reservationRepo = reservationRepo;
        this.itemRepo = itemRepo;
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
        List<Notification> sorted = repo.findByUserId(userId).stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
        currentUserNotifications.setAll(sorted);
    }

    public ObservableList<Notification> getCurrentUserNotifications() {
        return currentUserNotifications;
    }

    public Notification push(String userId, String title, String message, NotificationType type) {
        Notification n = new Notification(userId, title, message, type);
        repo.save(n);
        if (userId.equals(currentUserId)) {
            Platform.runLater(() -> currentUserNotifications.add(0, n));
        }
        return n;
    }

    public Notification pushTagged(String userId, String title, String message,
            NotificationType type, String tag) {
        if (repo.existsByUserAndTag(userId, tag)) {
            return null;
        }
        Notification n = new Notification(userId, title, message, type);
        n.setTag(tag);
        repo.save(n);
        if (userId.equals(currentUserId)) {
            Platform.runLater(() -> currentUserNotifications.add(0, n));
        }
        return n;
    }


    public List<Notification> getForUser(String userId) {
        return repo.findByUserId(userId).stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return repo.findByUserId(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    public void markAllRead(String userId) {
        List<Notification> unread = repo.findByUserId(userId).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        for (Notification n : unread) {
            n.setRead(true);
            repo.save(n);
        }
        if (userId.equals(currentUserId)) {
            Platform.runLater(() -> {
                currentUserNotifications.forEach(n -> n.setRead(true));
                List<Notification> copy = List.copyOf(currentUserNotifications);
                currentUserNotifications.setAll(copy);
            });
        }
    }

    public void clearAll(String userId) {
        repo.deleteAllByUserId(userId);
        if (userId.equals(currentUserId)) {
            Platform.runLater(currentUserNotifications::clear);
        }
    }

    public void scanDueSoonBorrows(String userId, int daysThreshold) {
        LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
        borrowHistoryRepo.findActiveByUserId(userId).stream()
                .filter(r -> r.getStatus() == BorrowStatus.ACTIVE
                        || r.getStatus() == BorrowStatus.EXTENDED)
                .filter(r -> !r.getDueDate().isAfter(threshold))
                .forEach(r -> {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), r.getDueDate());
                    String tag = "due-soon-" + r.getRecordId() + "-" + LocalDate.now();
                    String title = daysLeft <= 0 ? "Overdue Item " : "Return Reminder ";
                    String msg = daysLeft <= 0
                            ? "An item you borrowed is overdue! Please return it immediately."
                            : "Due in " + daysLeft + " day(s) on " + r.getDueDate()
                                    + ". Please return it on time.";
                    pushTagged(userId, title, msg, NotificationType.WARNING, tag);
                });
    }

    public void scanAvailableReservations(String userId) {
        reservationRepo.findByUserId(userId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.NOTIFIED)
                .filter(r -> r.getClaimDeadline() != null
                        && r.getClaimDeadline().isAfter(LocalDateTime.now()))
                .forEach(r -> {
                    String tag = "res-available-" + r.getReservationId();
                    var item = itemRepo.findById(r.getItemId());
                    String itemTitle = item != null ? "\"" + item.getTitle() + "\"" : "Your reserved item";
                    String msg = itemTitle + " is ready for pickup! Collect before "
                            + r.getClaimDeadline().toLocalDate() + ".";
                    pushTagged(userId, "Reservation Ready 📖", msg, NotificationType.INFO, tag);
                });
    }

    public void performLoginScan(String userId) {
        scanDueSoonBorrows(userId, 3);
        scanAvailableReservations(userId);
    }
}
