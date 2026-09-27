package ir.ac.kntu.services;

import java.util.concurrent.ThreadLocalRandom;

import ir.ac.kntu.entities.module.Guest;
import ir.ac.kntu.entities.module.Professor;
import ir.ac.kntu.entities.module.Student;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.exceptions.AccountDisabledException;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.ServiceResult;
import ir.ac.kntu.util.Validator;

public class AuthService {

    private static final String BORROW_LIMIT_MSG = " Borrow limit: ";
    private static final String ITEMS_SUFFIX = " items.";

    private final UserRepo userRepo;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public String generateGateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(100_000, 999_999));
    }

    public ServiceResult<User> signIn(String identifier, String password) {
        User user = userRepo.findByEmail(identifier);
        if (user == null) {
            user = userRepo.findByPhone(identifier);
        }
        if (user == null) {
            user = userRepo.findByUsername(identifier);
        }
        if (user != null && !user.isActive()) {
            throw new AccountDisabledException(user.getUserName());
        }
        if (user != null && user.getPassword().equals(password)) {
            return ServiceResult.success(user, "Welcome Back " + user.getUserName());
        }
        return ServiceResult.failure("Invalid identifier or password.");
    }

    public ServiceResult<User> registerGuest(String username, String email, String phone, String password) {
        ServiceResult<Void> validation = validateRegistration(username, email, password, phone);
        if (!validation.isSuccess()) {
            return ServiceResult.failure(validation.getMessage());
        }
        Guest guest = new Guest(username, email, password, phone);
        userRepo.save(guest);
        return ServiceResult.success(guest,
                "Guest account created successfully!" + BORROW_LIMIT_MSG + guest.getMaxBorrowLimit() + ITEMS_SUFFIX);
    }

    public ServiceResult<User> registerStudent(String username, String email, String password, String combined) {
        String[] parts = combined.split("::");
        String phone = parts[0];
        String studentId = parts.length > 1 ? parts[1] : null;
        if (studentId == null || studentId.trim().isEmpty()) {
            return ServiceResult.failure("Student ID cannot be empty.");
        }
        ServiceResult<Void> validation = validateRegistration(username, email, password, phone);
        if (!validation.isSuccess()) {
            return ServiceResult.failure(validation.getMessage());
        }
        Student student = new Student(username, email, password, studentId, phone);
        userRepo.save(student);
        return ServiceResult.success(student,
                "Student account created successfully!" + BORROW_LIMIT_MSG + student.getMaxBorrowLimit() + ITEMS_SUFFIX);
    }

    public ServiceResult<User> registerProfessor(String username, String email, String password, String combined) {
        String[] parts = combined.split("::");
        String phone = parts[0];
        String department = parts.length > 1 ? parts[1] : null;
        if (department == null || department.trim().isEmpty()) {
            return ServiceResult.failure("Department cannot be empty.");
        }
        ServiceResult<Void> validation = validateRegistration(username, email, password, phone);
        if (!validation.isSuccess()) {
            return ServiceResult.failure(validation.getMessage());
        }
        Professor professor = new Professor(username, email, password, department, phone);
        userRepo.save(professor);
        return ServiceResult.success(professor,
                "Professor account created successfully!" + BORROW_LIMIT_MSG + professor.getMaxBorrowLimit() + ITEMS_SUFFIX);
    }

    private ServiceResult<Void> validateRegistration(String username, String email, String password, String phone) {
        Validator validator = new Validator();
        if (!validator.isValidUsername(username)) {
            return ServiceResult.failure("Invalid username (3-20 chars, alphanumeric/underscores).");
        }
        if (!validator.isValidEmail(email)) {
            return ServiceResult.failure("Invalid email format.");
        }
        if (!validator.isValidPhone(phone)) {
            return ServiceResult.failure("Invalid phone number format.");
        }
        if (!validator.isStrongPassword(password)) {
            return ServiceResult.failure("Password too weak (8+ chars, upper, lower, number, special char).");
        }
        if (userRepo.findByUsername(username) != null) {
            return ServiceResult.failure("Username already taken.");
        }
        if (userRepo.findByEmail(email) != null) {
            return ServiceResult.failure("Email already exists.");
        }
        if (userRepo.findByPhone(phone) != null) {
            return ServiceResult.failure("Phone number already exists.");
        }
        return ServiceResult.success("Valid");
    }
}
