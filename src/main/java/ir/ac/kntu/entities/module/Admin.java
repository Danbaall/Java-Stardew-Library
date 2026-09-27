package ir.ac.kntu.entities.module;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;

@JsonTypeName("ADMIN")
public class Admin extends User implements Manageable, TicketHandler {

    private List<TicketType> assignedTypes;

    public Admin() {
        this.assignedTypes = new ArrayList<>();
    }

    public Admin(String userName, String password, String email, String phoneNumber) {
        super(userName, password, email, Role.ADMIN, phoneNumber);
        this.assignedTypes = new ArrayList<>();
    }

    @Override
    public List<TicketType> getAssignedTypes() {
        return assignedTypes;
    }

    public void setAssignedTypes(List<TicketType> assignedTypes) {
        this.assignedTypes = (assignedTypes != null) ? assignedTypes : new ArrayList<>();
    }

    @Override
    public boolean canHandle(TicketType type) {
        return assignedTypes == null || assignedTypes.isEmpty() || assignedTypes.contains(type);
    }

    @Override
    public String getSummary() {
        return "ADMIN | " + getUserName() + " | " + getId()
                + " | " + getEmail() + " | types=" + assignedTypes
                + " | active=" + isActive();
    }
}
