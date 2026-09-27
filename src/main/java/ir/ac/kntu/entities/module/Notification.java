package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.NotificationType;
import ir.ac.kntu.util.IdGenerator;

import java.time.LocalDateTime;

public class Notification {

    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime timestamp;
    private boolean read;
    /** Optional deduplication tag, e.g. "due-soon-brw-XYZ" or "res-available-res-ABC". */
    private String tag;

    public Notification() {
        // for Jackson
    }

    public Notification(String userId, String title, String message, NotificationType type) {
        this.id = IdGenerator.generateNotificationId();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @Override
    public String toString() {
        return "Notification[id=" + id + ", userId=" + userId + ", type=" + type
                + ", title=" + title + ", read=" + read + "]";
    }
}
