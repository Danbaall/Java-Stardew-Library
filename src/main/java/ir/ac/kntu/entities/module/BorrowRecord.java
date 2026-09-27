package ir.ac.kntu.entities.module;

import java.time.LocalDate;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.util.IdGenerator;

public class BorrowRecord {

    private String recordId;
    private String userId;
    private String itemId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private BorrowStatus status;
    private int extensionCount; // can only ask for extension once but can be updated later

    private double debtAmount;
    private boolean debtPaid;

    public BorrowRecord() {
        //this is for jackson
    }

    public BorrowRecord(String userId, String itemId, LocalDate dueDate) {
        recordId = IdGenerator.generateBorrowId();
        this.userId = userId;
        this.itemId = itemId;
        this.borrowDate = LocalDate.now();
        this.dueDate = dueDate;
        this.extensionCount = 0;
        this.status = BorrowStatus.ACTIVE;
        this.debtAmount = 0;
        this.debtPaid = false;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public int getExtensionCount() {
        return extensionCount;
    }

    public boolean extendDueDate(int extraDays) {
        if (this.extensionCount < 1 && this.status == BorrowStatus.ACTIVE) {
            this.dueDate = this.dueDate.plusDays(extraDays);
            this.extensionCount++;
            this.status = BorrowStatus.EXTENDED;
            return true;
        }
        return false;
    }

    public void markReturned() {
        this.status = BorrowStatus.RETURNED;
    }

    public void markOverdue() {
        if (this.status == BorrowStatus.ACTIVE || this.status == BorrowStatus.EXTENDED) {
            this.status = BorrowStatus.OVERDUE;
        }
    }

    public double getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(double debtAmount) {
        this.debtAmount = debtAmount;
    }

    public boolean isDebtPaid() {
        return debtPaid;
    }

    public void setDebtPaid(boolean debtPaid) {
        this.debtPaid = debtPaid;
    }

    @Override
    public String toString() {
        return "BorrowRecord [recordId=" + recordId + ", userId=" + userId + ", itemId=" + itemId
                + ", borrowDate=" + borrowDate + ", dueDate=" + dueDate + ", status=" + status
                + ", debtAmount=" + debtAmount + ", debtPaid=" + debtPaid + "]";
    }

}
