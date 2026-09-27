package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;
import ir.ac.kntu.entities.enums.Role;

@JsonTypeName("STUDENT")
public class Student extends Client {

    private String studentId;

    public Student() {
        //this is for jackson
    }

    public Student(
        String username,
        String email,
        String password,
        String studentId,
        String phoneNumber
    ) {
        super(username, email, password, Role.STUDENT, phoneNumber);
        this.studentId = studentId;
    }

    @Override
    public int getMaxBorrowLimit() {
        return LibraryPolicy.getInstance().getBorrowLimit(Role.STUDENT);
    }

    @Override
    public int getMaxBorrowDays() {
        return LibraryPolicy.getInstance().getBorrowDays(Role.STUDENT);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return super.toString() + " [studentId=" + studentId + "]";
    }
}
