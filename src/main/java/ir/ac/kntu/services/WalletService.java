package ir.ac.kntu.services;

import ir.ac.kntu.entities.enums.TransactionType;
import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.Transaction;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.TransactionRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.ServiceResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WalletService {

    private final UserRepo userRepo;
    private final TransactionRepo transactionRepo;
    private final DebtService debtService;
    private final BorrowHistoryRepo borrowRepo;

    public WalletService(
            UserRepo userRepo,
            TransactionRepo transactionRepo,
            DebtService debtService,
            BorrowHistoryRepo borrowRepo) {
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
        this.debtService = debtService;
        this.borrowRepo = borrowRepo;
    }

    public ServiceResult<Void> chargeWallet(Client client, double amount) {
        if (amount <= 0) {
            return ServiceResult.failure("Amount must be positive.");
        }
        client.getWallet().addFunds(amount);
        Transaction txn = new Transaction(
                client.getId(),
                amount,
                TransactionType.CHARGE,
                LocalDateTime.now(),
                "Wallet Charge");
        userRepo.save(client);
        transactionRepo.save(txn);
        return ServiceResult.success(
                "Wallet charged. New balance: " + client.getWallet().getBalance());
    }

    public ServiceResult<Void> payDebt(Client client, double amount) {
        List<BorrowRecord> unpaidRecords = debtService.getUnpaidDebtRecords(
                client.getId());
        if (unpaidRecords.isEmpty()) {
            return ServiceResult.failure("You have no standing debts.");
        }
        double currentDebt = debtService.calculateUserDebtRaw(client.getId());
        if (amount > currentDebt) {
            return ServiceResult.failure(
                    "Payment amount exceeds current debt.");
        }
        if (client.getWallet().getBalance() < amount) {
            return ServiceResult.failure("Insufficient funds.");
        }
        client.getWallet().deductFunds(amount);
        applyPaymentToRecords(unpaidRecords, amount);
        return ServiceResult.success(
                "Payment of " + amount + " applied successfully.");
    }

    private void applyPaymentToRecords(
            List<BorrowRecord> unpaidRecords,
            double remainingPayment) {
        double remaining = remainingPayment;
        double fineRate = LibraryPolicy.getInstance().getFineRatePerDay();
        for (BorrowRecord record : unpaidRecords) {
            if (remaining <= 0) {
                break;
            }
            double recordDebt = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now()) *
                    fineRate;
            record.setDebtAmount(recordDebt);
            record.setDebtPaid(true);
            remaining -= Math.min(remaining, recordDebt);
            borrowRepo.save(record);
        }
    }

    public java.util.List<Transaction> getTransactionHistory(String userId) {
        return transactionRepo.findByUserId(userId).stream()
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .collect(java.util.stream.Collectors.toList());
    }
}
