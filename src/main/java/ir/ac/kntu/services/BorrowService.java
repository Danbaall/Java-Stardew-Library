package ir.ac.kntu.services;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.PhysicalItem;
import ir.ac.kntu.entities.module.Reservation;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.util.ServiceResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BorrowService {

    private final BorrowHistoryRepo borrowRepo;
    private final ItemRepo itemRepo;
    private final DebtService debtService;
    private final ReservationService reservationService;

    public BorrowService(
            BorrowHistoryRepo borrowRepo,
            ItemRepo itemRepo,
            DebtService debtService,
            ReservationService reservationService) {
        this.borrowRepo = borrowRepo;
        this.itemRepo = itemRepo;
        this.debtService = debtService;
        this.reservationService = reservationService;
    }

    public ServiceResult<Void> checkOut(Client patron, String itemId) {
        if (!checkDebts(patron)) {
            return ServiceResult.failure("Please pay off your debt first before borrowing.");
        }

        if (!checkBorrowLimit(patron)) {
            return ServiceResult.failure("You can't borrow any more items!");
        }

        Item item = itemRepo.findById(itemId);
        if (item == null) {
            return ServiceResult.failure("Item not found.");
        }

        if (item instanceof PhysicalItem physical) {
            reservationService.processExpiredReservations(physical);
        }

        boolean isReservationBorrow = checkReservationBorrow(patron, itemId);
        if (!isReservationBorrow) {
            if (!item.checkout()) {
                return ServiceResult.failure("Item is currently out of stock.");
            }
            itemRepo.save(item);
        }

        LocalDate dueDate = patron.calculateDueDate();
        BorrowRecord record = new BorrowRecord(patron.getId(), item.getId(), dueDate);
        borrowRepo.save(record);

        if (isReservationBorrow) {
            fulfillReservation(patron, itemId);
        }

        return ServiceResult.success("Successfully borrowed! Please return by " + dueDate);
    }

    private boolean checkDebts(Client patron) {
        List<BorrowRecord> unpaidDebts = debtService.getUnpaidDebtRecords(patron.getId());
        return unpaidDebts.isEmpty();
    }

    private boolean checkBorrowLimit(Client patron) {
        List<BorrowRecord> active = borrowRepo.findActiveByUserId(patron.getId());
        return patron.canBorrow(active.size());
    }

    private boolean checkReservationBorrow(Client patron, String itemId) {
        Item item = itemRepo.findById(itemId);
        if (!(item instanceof PhysicalItem)) {
            return false;
        }
        Reservation res = reservationService.getActiveReservationForUserAndItem(
                patron.getId(), itemId);
        return res != null
                && res.getClaimDeadline() != null
                && res.getClaimDeadline().isAfter(LocalDateTime.now());
    }

    private void fulfillReservation(Client patron, String itemId) {
        Reservation res = reservationService.getActiveReservationForUserAndItem(
                patron.getId(), itemId);
        if (res != null) {
            reservationService.fulfillReservation(res.getReservationId());
        }
    }

    public ServiceResult<Void> returnItem(String recordId, User currentUser) {
        BorrowRecord record = borrowRepo.findById(recordId);
        if (record == null) {
            return ServiceResult.failure("Record not found.");
        }

        boolean isPrivileged = currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.MANAGER;
        if (!record.getUserId().equals(currentUser.getId()) && !isPrivileged) {
            return ServiceResult.failure(
                    "You do not have permission to return this item.");
        }

        if (record.getStatus() == BorrowStatus.RETURNED) {
            return ServiceResult.failure("Item already returned.");
        }

        Item item = itemRepo.findById(record.getItemId());
        if (item != null) {
            item.returnItem();
            itemRepo.save(item);
        }

        record.markReturned();
        borrowRepo.save(record);

        if (item instanceof PhysicalItem physical) {
            reservationService.handleItemReturned(physical);
        }

        return ServiceResult.success("Item returned successfully.");
    }

    public ServiceResult<Void> requestExtension(
            String borrowId,
            User currentUser) {
        BorrowRecord record = borrowRepo.findById(borrowId);
        if (record == null) {
            return ServiceResult.failure("Record not found.");
        }
        if (!record.getUserId().equals(currentUser.getId())) {
            return ServiceResult.failure("This is not your borrow record.");
        }

        boolean extended = record.extendDueDate(7);
        if (!extended) {
            return ServiceResult.failure(
                    "Extension not possible (already extended or not active).");
        }

        borrowRepo.save(record);
        return ServiceResult.success("Due date extended by 7 days.");
    }

    public ServiceResult<List<BorrowRecord>> getUserBorrowHistory(
            Client patron) {
        List<BorrowRecord> records = borrowRepo.findByUserId(patron.getId());
        return ServiceResult.success(
                records,
                "Found " + records.size() + " records.");
    }

    public ServiceResult<List<BorrowRecord>> getGeneralHistory(
            User currentUser) {
        if (currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.MANAGER) {
            return ServiceResult.failure(
                    "Only admins and the manager can view all borrow records.");
        }
        List<BorrowRecord> records = borrowRepo.findAll();
        return ServiceResult.success(
                records,
                "Found " + records.size() + " global records.");
    }
}
