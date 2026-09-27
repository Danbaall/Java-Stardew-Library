package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.util.IdGenerator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
        property = "role", defaultImpl = Admin.class, visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Admin.class,     name = "ADMIN"),
    @JsonSubTypes.Type(value = Manager.class,   name = "MANAGER"),
    @JsonSubTypes.Type(value = Guest.class,     name = "GUEST"),
    @JsonSubTypes.Type(value = Student.class,   name = "STUDENT"),
    @JsonSubTypes.Type(value = Professor.class, name = "PROFESSOR")
})
public class User {

    private static final String SEP = " | ";

    private String userName;
    private String email;
    private String id;
    private String password;
    private Role role;
    private String phoneNumber;
    private boolean active = true;

    public User() {
        //this is for jackson
    }

    public User(String userName, String password, String email, Role role, String phoneNumber) {
        this.userName = userName;
        this.password = password;
        this.id = IdGenerator.generateUserId(role);
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.active = true;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSummary() {
        return getRole() + SEP + getUserName() + SEP + getId()
                + SEP + getEmail() + " | active=" + active;
    }

    @Override
    public String toString() {
        return "User [userName=" + userName + ", email=" + email
                + ", id=" + id + ", role=" + role
                + ", phoneNumber=" + phoneNumber + ", active=" + active + "]";
    }
}
