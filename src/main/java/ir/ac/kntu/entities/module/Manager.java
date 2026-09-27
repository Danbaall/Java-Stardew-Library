package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ir.ac.kntu.entities.enums.Role;

@JsonTypeName("MANAGER")
public class Manager extends User implements Manageable {

    public Manager() {
        //this is for jackson
    }

    public Manager(String userName, String password, String email, String phoneNumber) {
        super(userName, password, email, Role.MANAGER, phoneNumber);
    }

    @Override
    public String getSummary() {
        return "MANAGER | " + getUserName() + " | " + getId()
                + " | " + getEmail() + " | active=" + isActive();
    }
}
