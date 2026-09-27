package ir.ac.kntu.services;

import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.ServiceResult;
import ir.ac.kntu.util.Validator;

public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public ServiceResult<Void> updateUsername(User user, String newUsername) {
        Validator validator = new Validator();
        if (!validator.isValidUsername(newUsername)) {
            return ServiceResult.failure("Invalid username (3-20 chars, alphanumeric/underscores).");
        }
        User existing = userRepo.findByUsername(newUsername);
        if (existing != null && !existing.getId().equals(user.getId())) {
            return ServiceResult.failure("Username already taken.");
        }
        user.setUserName(newUsername);
        userRepo.save(user);
        return ServiceResult.success("Username updated successfully.");
    }

    public ServiceResult<Void> updateEmail(User user, String newEmail) {
        Validator validator = new Validator();
        if (!validator.isValidEmail(newEmail)) {
            return ServiceResult.failure("Invalid email format.");
        }
        User existingUser = userRepo.findByEmail(newEmail);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            return ServiceResult.failure(
                    "Email is already used by another account.");
        }
        user.setEmail(newEmail);
        userRepo.save(user);
        return ServiceResult.success("Email updated successfully.");
    }

    public User findUserById(String id) {
        return userRepo.findById(id);
    }

    public ServiceResult<Void> updatePhone(User user, String newPhone) {
        Validator validator = new Validator();
        if (!validator.isValidPhone(newPhone)) {
            return ServiceResult.failure("Invalid phone number format.");
        }
        user.setPhoneNumber(newPhone);
        userRepo.save(user);
        return ServiceResult.success("Phone number updated successfully.");
    }

    public ServiceResult<Void> updatePassword(
            User user,
            String oldPassword,
            String newPassword) {
        Validator validator = new Validator();
        if (!user.getPassword().equals(oldPassword)) {
            return ServiceResult.failure("Current password is incorrect.");
        }
        if (!validator.isStrongPassword(newPassword)) {
            return ServiceResult.failure("New password is too weak.");
        }
        user.setPassword(newPassword);
        userRepo.save(user);
        return ServiceResult.success("Password updated successfully.");
    }
}
