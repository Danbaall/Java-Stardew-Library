package ir.ac.kntu.entities.module;

import java.time.LocalDateTime;

public record Message(
    String senderId,
    String msg,
    LocalDateTime timestamp
) {
    public Message(String senderId, String content) {
        this(senderId, content, LocalDateTime.now());
    }
}
