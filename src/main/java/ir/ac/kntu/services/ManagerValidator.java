package ir.ac.kntu.services;

import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.exceptions.DuplicateUserException;
import ir.ac.kntu.exceptions.InvalidInputException;
import ir.ac.kntu.exceptions.PolicyViolationException;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.Validator;

public class ManagerValidator {

    private final UserRepo userRepo;
    private final Validator validator;

    public ManagerValidator(UserRepo userRepo) {
        this.userRepo = userRepo;
        this.validator = new Validator();
    }

    public void validateInputFormat(String username, String password, String email, String phone) {
        if (!validator.isValidUsername(username)) {
            throw new InvalidInputException("Invalid username (3-20 chars, alphanumeric/underscores).");
        }
        if (!validator.isStrongPassword(password)) {
            throw new InvalidInputException("Password too weak (8+ chars, upper, lower, number, special char).");
        }
        if (!validator.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format.");
        }
        if (!validator.isValidPhone(phone)) {
            throw new InvalidInputException("Invalid phone number format.");
        }
    }

    public void validateUniqueness(String username, String email, String phone) {
        if (userRepo.findByUsername(username) != null) {
            throw new DuplicateUserException("Username", username);
        }
        if (userRepo.findByEmail(email) != null) {
            throw new DuplicateUserException("Email", email);
        }
        if (userRepo.findByPhone(phone) != null) {
            throw new DuplicateUserException("Phone", phone);
        }
    }

    public void validateFieldEdit(User target, String field, String value, String userId) {
        switch (field) {
            case "username" -> validateUsernameEdit(target, value, userId);
            case "email" -> validateEmailEdit(target, value, userId);
            case "phone" -> validatePhoneEdit(target, value, userId);
            default -> throw new InvalidInputException("Unknown field '" + field + "'.");
        }
    }

    public void validatePolicy(LibraryPolicy policy) {
        validateFineRate(policy);
        validateBorrowDays(policy);
        validateBorrowLimits(policy);
        validateReservationCounts(policy);
        if (policy.getReservationPickupDays() < 1) {
            throw new PolicyViolationException("Pickup window must be at least 1 day.");
        }
    }

    private void validateUsernameEdit(User target, String value, String userId) {
        if (!validator.isValidUsername(value)) {
            throw new InvalidInputException("Invalid username.");
        }
        User existing = userRepo.findByUsername(value);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new DuplicateUserException("Username", value);
        }
        target.setUserName(value);
    }

    private void validateEmailEdit(User target, String value, String userId) {
        if (!validator.isValidEmail(value)) {
            throw new InvalidInputException("Invalid email format.");
        }
        User existing = userRepo.findByEmail(value);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new DuplicateUserException("Email", value);
        }
        target.setEmail(value);
    }

    private void validatePhoneEdit(User target, String value, String userId) {
        if (!validator.isValidPhone(value)) {
            throw new InvalidInputException("Invalid phone number format.");
        }
        User existing = userRepo.findByPhone(value);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new DuplicateUserException("Phone", value);
        }
        target.setPhoneNumber(value);
    }

    private void validateFineRate(LibraryPolicy policy) {
        if (policy.getFineRatePerDay() < 0) {
            throw new PolicyViolationException("Fine rate cannot be negative.");
        }
    }

    private void validateBorrowDays(LibraryPolicy policy) {
        if (policy.getStudentMaxBorrowDays() < 1
                || policy.getProfessorMaxBorrowDays() < 1
                || policy.getGuestMaxBorrowDays() < 1) {
            throw new PolicyViolationException("Max borrow days must be at least 1 for all roles.");
        }
        if (policy.getMaxReserveDays() < 1) {
            throw new PolicyViolationException("Max reserve days must be at least 1.");
        }
    }

    private void validateBorrowLimits(LibraryPolicy policy) {
        if (policy.getStudentMaxBorrowLimit() < 1
                || policy.getProfessorMaxBorrowLimit() < 1
                || policy.getGuestMaxBorrowLimit() < 1) {
            throw new PolicyViolationException("Borrow limit must be at least 1 for all roles.");
        }
    }

    private void validateReservationCounts(LibraryPolicy policy) {
        if (policy.getStudentMaxReservations() < 0
                || policy.getProfessorMaxReservations() < 0
                || policy.getGuestMaxReservations() < 0) {
            throw new PolicyViolationException("Max reservations cannot be negative.");
        }
    }
}
