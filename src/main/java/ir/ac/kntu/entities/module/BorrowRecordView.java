package ir.ac.kntu.entities.module;

import java.time.LocalDate;

import ir.ac.kntu.entities.enums.BorrowStatus;

public record BorrowRecordView(String recordId, String userName,
        String itemTitle, LocalDate borrowDate,
        LocalDate dueDate, BorrowStatus status) {

    @Override
    public String toString() {
        return "BorrowRecord [recordId=" + recordId + ", user=" + userName + ", item=" + itemTitle + ", borrowDate="
                + borrowDate + ", dueDate=" + dueDate + ", status=" + status + "]";
    }

}
