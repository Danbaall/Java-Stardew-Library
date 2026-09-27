package ir.ac.kntu.entities.module;

import java.time.LocalDateTime;

import ir.ac.kntu.entities.enums.ReservationStatus;
import ir.ac.kntu.util.IdGenerator;

public class Reservation {
    private String reservationId;
    private String userId;
    private String itemId;
    private LocalDateTime reservedAt;
    private ReservationStatus status;
    private LocalDateTime notifiedAt;
    private LocalDateTime claimDeadline;

    public Reservation() {
        /* Jackson */
    }

    public Reservation(String userId, String itemId) {
        this.reservationId = IdGenerator.generateReservationId();
        this.userId = userId;
        this.itemId = itemId;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.WAITING;
    }

    public void notifyUser(int pickupDays) {
        this.status = ReservationStatus.NOTIFIED;
        this.notifiedAt = LocalDateTime.now();
        this.claimDeadline = notifiedAt.plusDays(pickupDays);
    }

    public void fulfill() {
        this.status = ReservationStatus.FULFILLED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == ReservationStatus.WAITING || status == ReservationStatus.NOTIFIED;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public LocalDateTime getClaimDeadline() {
        return claimDeadline;
    }

    public void setReservationId(String id) {
        this.reservationId = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public void setClaimDeadline(LocalDateTime claimDeadline) {
        this.claimDeadline = claimDeadline;
    }

    @Override
    public String toString() {
        return "Reservation[id=" + reservationId + ", user=" + userId
                + ", item=" + itemId + ", status=" + status
                + ", deadline=" + claimDeadline + "]";
    }
}
