package ir.ac.kntu.entities.module;

import java.time.LocalDateTime;

import ir.ac.kntu.entities.enums.TransactionType;
import ir.ac.kntu.util.IdGenerator;

public record Transaction(String transactionId,
        String userId,
        double amount,
        TransactionType type,
        LocalDateTime timestamp,
        String description) {
    public Transaction(String userId, double amount, TransactionType type, LocalDateTime timestamp, String description) {
        this(IdGenerator.generateTransactionId().toString(), userId, amount,type, LocalDateTime.now(), description);
    }

    public String getId() {
        return transactionId;
    }
}
