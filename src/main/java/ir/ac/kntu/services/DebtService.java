package ir.ac.kntu.services;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.ServiceResult;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class DebtService {

    private final BorrowHistoryRepo borrowRepo;
    private final UserRepo userRepo;

    public DebtService(BorrowHistoryRepo borrowRepo, UserRepo userRepo) {
        this.borrowRepo = borrowRepo;
        this.userRepo = userRepo;
    }

    public ServiceResult<Double> calculateUserDebt(String userId) {
        List<BorrowRecord> records = borrowRepo.findByUserId(userId);
        LocalDate today = LocalDate.now();
        double fineRate = LibraryPolicy.getInstance().getFineRatePerDay();

        double totalDebt = records.stream()
                .mapToDouble(record -> calculateRecordDebt(record, today, fineRate))
                .sum();
        return ServiceResult.success(totalDebt, "Calculated debt.");
    }

    private double calculateRecordDebt(
            BorrowRecord record,
            LocalDate today,
            double fineRate) {
        boolean overdue = record.getStatus() == BorrowStatus.OVERDUE ||
                (record.getStatus() == BorrowStatus.ACTIVE &&
                        today.isAfter(record.getDueDate()));
        if (!record.isDebtPaid() && overdue) {
            long daysOverdue = ChronoUnit.DAYS.between(
                    record.getDueDate(),
                    today);
            if (daysOverdue > 0) {
                record.markOverdue();
                borrowRepo.save(record);
                return daysOverdue * fineRate;
            }
        }
        return 0.0;
    }

    public double calculateUserDebtRaw(String userId) {
        return calculateUserDebt(userId).getData();
    }

    public ServiceResult<List<User>> getUsersWithDebt(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.MANAGER) {
            return ServiceResult.failure("Admins and the manager only.");
        }

        List<User> allUsers = userRepo.findAll();
        List<User> debtors = allUsers.stream()
                .filter(u -> u instanceof Client)
                .filter(u -> calculateUserDebtRaw(u.getId()) > 0)
                .toList();
        return ServiceResult.success(
                debtors,
                "Found " + debtors.size() + " users with debt.");
    }

    public List<BorrowRecord> getUnpaidDebtRecords(String userId) {
        return borrowRepo
                .findByUserId(userId)
                .stream()
                .filter(
                        r -> (r.getStatus() == BorrowStatus.OVERDUE && !r.isDebtPaid())
                                || (r.getStatus() == BorrowStatus.ACTIVE &&
                                        LocalDate.now().isAfter(r.getDueDate())))
                .collect(Collectors.toList());
    }
}
