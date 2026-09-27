package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.Role;

public interface PolicyConfigurable {
    void setBorrowDays(Role role, int days);

    void setBorrowLimit(Role role, int limit);

    void setFineRatePerDay(double rate);

    void setMaxReservations(Role role, int count);

    void setMaxReserveDays(int days);
}
