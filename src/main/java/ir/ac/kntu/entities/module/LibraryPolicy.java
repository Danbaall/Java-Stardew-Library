package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.Role;

public class LibraryPolicy implements PolicyConfigurable {

    private static LibraryPolicy instance;

    private int studentMaxBorrowDays = 30;
    private int professorMaxBorrowDays = 40;
    private int guestMaxBorrowDays = 14;

    private int studentMaxBorrowLimit = 10;
    private int professorMaxBorrowLimit = 15;
    private int guestMaxBorrowLimit = 2;

    private double fineRatePerDay = 10.0;

    private int studentMaxReservations = 3;
    private int professorMaxReservations = 5;
    private int guestMaxReservations = 1;

    private int maxReserveDays = 30;

    private int reservationPickupDays = 2;

    public int getReservationPickupDays() {
        return reservationPickupDays;
    }

    public void setReservationPickupDays(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Pickup window must be at least 1 day.");
        }
        this.reservationPickupDays = days;
    }

    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public LibraryPolicy() {
        // this is for jackson
    }

    public static LibraryPolicy getInstance() {
        if (instance == null) {
            instance = new LibraryPolicy();
        }
        return instance;
    }

    public static void setInstance(LibraryPolicy policy) {
        instance = policy;
    }

    public int getBorrowDays(Role role) {
        return switch (role) {
            case STUDENT -> studentMaxBorrowDays;
            case PROFESSOR -> professorMaxBorrowDays;
            case GUEST -> guestMaxBorrowDays;
            default -> 14;
        };
    }

    public int getBorrowLimit(Role role) {
        return switch (role) {
            case STUDENT -> studentMaxBorrowLimit;
            case PROFESSOR -> professorMaxBorrowLimit;
            case GUEST -> guestMaxBorrowLimit;
            default -> 1;
        };
    }

    public int getMaxReservations(Role role) {
        return switch (role) {
            case STUDENT -> studentMaxReservations;
            case PROFESSOR -> professorMaxReservations;
            case GUEST -> guestMaxReservations;
            default -> 0;
        };
    }

    @Override
    public void setBorrowDays(Role role, int days) {
        switch (role) {
            case STUDENT -> studentMaxBorrowDays = days;
            case PROFESSOR -> professorMaxBorrowDays = days;
            case GUEST -> guestMaxBorrowDays = days;
            default -> {
            }
        }
    }

    @Override
    public void setBorrowLimit(Role role, int limit) {
        switch (role) {
            case STUDENT -> studentMaxBorrowLimit = limit;
            case PROFESSOR -> professorMaxBorrowLimit = limit;
            case GUEST -> guestMaxBorrowLimit = limit;
            default -> {
            }
        }
    }

    @Override
    public void setFineRatePerDay(double rate) {
        this.fineRatePerDay = rate;
    }

    @Override
    public void setMaxReservations(Role role, int count) {
        switch (role) {
            case STUDENT -> studentMaxReservations = count;
            case PROFESSOR -> professorMaxReservations = count;
            case GUEST -> guestMaxReservations = count;
            default -> {
            }
        }
    }

    @Override
    public void setMaxReserveDays(int days) {
        this.maxReserveDays = days;
    }

    public double getFineRatePerDay() {
        return fineRatePerDay;
    }

    public int getMaxReserveDays() {
        return maxReserveDays;
    }

    public int getStudentMaxBorrowDays() {
        return studentMaxBorrowDays;
    }

    public void setStudentMaxBorrowDays(int value) {
        this.studentMaxBorrowDays = value;
    }

    public int getProfessorMaxBorrowDays() {
        return professorMaxBorrowDays;
    }

    public void setProfessorMaxBorrowDays(int value) {
        this.professorMaxBorrowDays = value;
    }

    public int getGuestMaxBorrowDays() {
        return guestMaxBorrowDays;
    }

    public void setGuestMaxBorrowDays(int value) {
        this.guestMaxBorrowDays = value;
    }

    public int getStudentMaxBorrowLimit() {
        return studentMaxBorrowLimit;
    }

    public void setStudentMaxBorrowLimit(int value) {
        this.studentMaxBorrowLimit = value;
    }

    public int getProfessorMaxBorrowLimit() {
        return professorMaxBorrowLimit;
    }

    public void setProfessorMaxBorrowLimit(int value) {
        this.professorMaxBorrowLimit = value;
    }

    public int getGuestMaxBorrowLimit() {
        return guestMaxBorrowLimit;
    }

    public void setGuestMaxBorrowLimit(int value) {
        this.guestMaxBorrowLimit = value;
    }

    public int getStudentMaxReservations() {
        return studentMaxReservations;
    }

    public void setStudentMaxReservations(int value) {
        this.studentMaxReservations = value;
    }

    public int getProfessorMaxReservations() {
        return professorMaxReservations;
    }

    public void setProfessorMaxReservations(int value) {
        this.professorMaxReservations = value;
    }

    public int getGuestMaxReservations() {
        return guestMaxReservations;
    }

    public void setGuestMaxReservations(int value) {
        this.guestMaxReservations = value;
    }
}
