package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ir.ac.kntu.entities.enums.Role;

@JsonTypeName("PROFESSOR")
public class Professor extends Client {

    private String department;

    public Professor() {
        //this is for jackson
    }

    public Professor(
        String name,
        String email,
        String password,
        String department,
        String phoneNumber
    ) {
        super(name, email, password, Role.PROFESSOR, phoneNumber);
        this.department = department;
    }

    @Override
    public int getMaxBorrowLimit() {
        return LibraryPolicy.getInstance().getBorrowLimit(Role.PROFESSOR);
    }

    @Override
    public int getMaxBorrowDays() {
        return LibraryPolicy.getInstance().getBorrowDays(Role.PROFESSOR);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return super.toString() + " [department=" + department + "]";
    }
}
