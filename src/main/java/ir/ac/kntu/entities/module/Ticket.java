package ir.ac.kntu.entities.module;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ir.ac.kntu.entities.enums.TicketStatus;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.util.IdGenerator;

public class Ticket {
    private TicketType ticketType;
    private String ticketId;
    private String userId;
    private String subject;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private List<Message> messages;

    public Ticket() {
        //this is for jackson
    }

    public Ticket(TicketType ticketType, String userId, String subject, String initialMessage) {
        this.ticketType = ticketType;
        this.ticketId = IdGenerator.generateTicketId();
        this.userId = userId;
        this.subject = subject;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.messages = new ArrayList<>();
        this.addMessage(new Message(userId, initialMessage));
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        if (this.status == TicketStatus.OPEN) {
            this.status = TicketStatus.RESOLVING;
        }
    }

    public void closeTicket() {
        this.status = TicketStatus.CLOSED;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    @Override
    public String toString() {
        return "Ticket [ticketType=" + ticketType + ", ticketId=" + ticketId + ", userId=" + userId + ", subject="
                + subject + ", status=" + status + ", createdAt=" + createdAt + ", messages=" + messages + "]";
    }

}
