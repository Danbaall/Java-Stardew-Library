package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ir.ac.kntu.entities.enums.Role;

@JsonTypeName("GUEST")
public class Guest extends Client {

    public Guest() {
        //this is for jackson
    }

    public Guest(
        String name,
        String email,
        String password,
        String phoneNumber
    ) {
        super(name, email, password, Role.GUEST, phoneNumber);
    }

    @Override
    public int getMaxBorrowLimit() {
        return LibraryPolicy.getInstance().getBorrowLimit(Role.GUEST);
    }

    @Override
    public int getMaxBorrowDays() {
        return LibraryPolicy.getInstance().getBorrowDays(Role.GUEST);
    }
}
