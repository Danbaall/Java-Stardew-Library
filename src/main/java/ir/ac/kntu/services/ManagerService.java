package ir.ac.kntu.services;

import java.util.ArrayList;
import java.util.List;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.entities.module.UserFilter;
import ir.ac.kntu.exceptions.InvalidInputException;
import ir.ac.kntu.exceptions.UnauthorizedAccessException;
import ir.ac.kntu.exceptions.UserNotFoundException;
import ir.ac.kntu.repo.PolicyRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.util.Validator;

public class ManagerService {

    private final UserRepo userRepo;
    private final PolicyRepo policyRepo;
    private final Validator validator;
    private final ManagerValidator managerValidator;

    public ManagerService(UserRepo userRepo, PolicyRepo policyRepo) {
        this.userRepo = userRepo;
        this.policyRepo = policyRepo;
        this.validator = new Validator();
        this.managerValidator = new ManagerValidator(userRepo);
    }

    public Admin createAdmin(User manager, String username, String credentials) {
        checkManager(manager);
        String[] parts = credentials.split("::");
        managerValidator.validateInputFormat(username, parts[0], parts[1], parts[2]);
        managerValidator.validateUniqueness(username, parts[1], parts[2]);
        Admin admin = new Admin(username, parts[0], parts[1], parts[2]);
        userRepo.save(admin);
        return admin;
    }

    public void removeAdmin(User manager, String adminId) {
        checkManager(manager);
        User target = userRepo.findById(adminId);
        if (target == null || target.getRole() != Role.ADMIN) {
            throw new UserNotFoundException(adminId);
        }
        userRepo.delete(adminId);
    }

    public void assignTicketTypes(User manager, String adminId, List<TicketType> types) {
        checkManager(manager);
        User target = userRepo.findById(adminId);
        if (target == null || target.getRole() != Role.ADMIN) {
            throw new UserNotFoundException(adminId);
        }
        Admin admin = (Admin) target;
        admin.setAssignedTypes(types != null ? types : new ArrayList<>());
        userRepo.save(admin);
    }

    public List<User> listUsers(User manager, UserFilter filter) {
        checkManager(manager);
        return userRepo.findAll().stream()
                .filter(filter::matches)
                .toList();
    }

    public void editUserField(User manager, String userId, String field, String value) {
        checkManager(manager);
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException("Value cannot be empty.");
        }
        User target = requireUser(userId);
        if (target.getRole() == Role.ADMIN) {
            throw new InvalidInputException("Admins must be edited via the Admin Management section.");
        }
        managerValidator.validateFieldEdit(target, field, value, userId);
        userRepo.save(target);
    }

    public User getUserById(User manager, String userId) {
        checkManager(manager);
        return requireUser(userId);
    }

    public void editAdminField(User manager, String adminId, String field, String value) {
        checkManager(manager);
        User target = requireUser(adminId);
        if (target.getRole() != Role.ADMIN) {
            throw new InvalidInputException("User with ID '" + adminId + "' is not an admin.");
        }
        managerValidator.validateFieldEdit(target, field, value, adminId);
        userRepo.save(target);
    }

    public User getAdminById(User manager, String adminId) {
        checkManager(manager);
        User target = requireUser(adminId);
        if (target.getRole() != Role.ADMIN) {
            throw new InvalidInputException("User with ID '" + adminId + "' is not an admin.");
        }
        return target;
    }

    public void setUserActive(User manager, String userId, boolean active) {
        checkManager(manager);
        if (userId.equals(manager.getId())) {
            throw new InvalidInputException("Manager cannot change their own active status.");
        }
        User target = requireUser(userId);
        target.setActive(active);
        userRepo.save(target);
    }

    public void recoverPassword(User manager, String userId, String newPassword) {
        checkManager(manager);
        if (userId.equals(manager.getId())) {
            throw new InvalidInputException("You cannot change your own password via this function. "
                    + "Use Profile settings instead.");
        }
        User target = requireUser(userId);
        if (!validator.isStrongPassword(newPassword)) {
            throw new InvalidInputException("Password too weak (8+ chars, upper, lower, number, special char).");
        }
        target.setPassword(newPassword);
        userRepo.save(target);
    }

    public LibraryPolicy getPolicy(User manager) {
        checkManager(manager);
        return policyRepo.get();
    }

    public void updatePolicy(User manager, LibraryPolicy updated) {
        checkManager(manager);
        managerValidator.validatePolicy(updated);
        policyRepo.save(updated);
    }

    private void checkManager(User user) {
        if (user == null || user.getRole() != Role.MANAGER) {
            throw new UnauthorizedAccessException("Only the manager can perform this action.");
        }
    }

    private User requireUser(String userId) {
        User target = userRepo.findById(userId);
        if (target == null) {
            throw new UserNotFoundException(userId);
        }
        return target;
    }
}
