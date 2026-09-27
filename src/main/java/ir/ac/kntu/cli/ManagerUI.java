package ir.ac.kntu.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.module.LibraryPolicy;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.entities.module.UserFilter;
import ir.ac.kntu.exceptions.LibraryException;
import ir.ac.kntu.exceptions.PolicyViolationException;
import ir.ac.kntu.services.ManagerService;

public class ManagerUI {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[0;36m";
    private static final String YELLOW = "\033[0;33m";
    private static final String GREEN = "\033[0;32m";
    private static final String DONE_PREFIX = "done, ";
    private static final String FAILED_PREFIX = "failed, ";
    private static final String STUDENT_LBL = "  Student:";
    private static final String PROFESSOR_LBL = "  Professor:";
    private static final String GUEST_LBL = "  Guest:";

    private final ManagerService managerService;
    private final ItemUI itemUI;
    private final Scanner scanner;

    public ManagerUI(ManagerService managerService, ItemUI itemUI, Scanner scanner) {
        this.managerService = managerService;
        this.itemUI = itemUI;
        this.scanner = scanner;
    }

    public void displayManagerDashboard(User manager) {
        String menu = GREEN + "1. Admin Management\n2. User Management\n3. Policy Settings\n4. Logout" + RESET;
        runMenu("Manager Dashboard", menu, "4", buildManagerDashboardActions(manager));
    }

    private Map<String, Runnable> buildManagerDashboardActions(User manager) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> displayAdminManagement(manager));
        actions.put("2", () -> displayUserManagement(manager));
        actions.put("3", () -> displayPolicySettings(manager));
        return actions;
    }

    private void displayAdminManagement(User manager) {
        String menu = GREEN + "1. List Admins  \n2. Create Admin  \n3. Edit Admin\n"
                + "4. Remove Admin  \n5. Assign Ticket Types  \n6. Back" + RESET;
        runMenu("Admin Management", menu, "6", buildAdminManagementActions(manager));
    }

    private Map<String, Runnable> buildAdminManagementActions(User manager) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> listAdmins(manager));
        actions.put("2", () -> createAdmin(manager));
        actions.put("3", () -> editAdminById(manager));
        actions.put("4", () -> removeAdmin(manager));
        actions.put("5", () -> assignTicketTypes(manager));
        return actions;
    }

    private void runMenu(String title, String menuText, String exitChoice, Map<String, Runnable> actions) {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            printBanner(title, 50);
            System.out.println(menuText);
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals(exitChoice)) {
                running = false;
            } else {
                Runnable action = actions.get(choice);
                if (action != null) {
                    action.run();
                }
            }
        }
    }

    private void listAdmins(User manager) {
        try {
            List<User> admins = managerService.listUsers(manager, new UserFilter(null, null, Role.ADMIN));
            List<String> lines = new ArrayList<>();
            for (User user : admins) {
                lines.add(user.getSummary());
            }
            int selected = itemUI.displayPagedList(lines, "Admins (" + lines.size() + ")");
            if (selected < 0) {
                return;
            }
            Admin selectedAdmin = (Admin) admins.get(selected);
            System.out.println(selectedAdmin.toString()
                    + ", assignedTypes=" + selectedAdmin.getAssignedTypes());
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
            scanner.nextLine();
        }
    }

    private void createAdmin(User manager) {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            Admin admin = managerService.createAdmin(manager, username, password + "::" + email + "::" + phone);
            System.out.println(DONE_PREFIX + "Admin created: " + admin.getUserName() + " (ID: " + admin.getId() + ")");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void editAdminById(User manager) {
        System.out.print("Admin ID: ");
        String adminId = scanner.nextLine().trim();

        try {
            managerService.getAdminById(manager, adminId);
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
            scanner.nextLine();
            return;
        }

        String menu = GREEN + "1. Username  \n2. Email  \n3. Phone  \n4. Back" + RESET;
        runMenu("Edit Admin : " + adminId, menu, "4", buildEditAdminActions(manager, adminId));
    }

    private Map<String, Runnable> buildEditAdminActions(User manager, String adminId) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> applyAdminFieldEdit(manager, adminId, "username"));
        actions.put("2", () -> applyAdminFieldEdit(manager, adminId, "email"));
        actions.put("3", () -> applyAdminFieldEdit(manager, adminId, "phone"));
        return actions;
    }

    private void applyAdminFieldEdit(User manager, String adminId, String field) {
        System.out.print("New value: ");
        String value = scanner.nextLine().trim();
        try {
            managerService.editAdminField(manager, adminId, field, value);
            System.out.println(DONE_PREFIX + field + " updated.");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void removeAdmin(User manager) {
        try {
            System.out.print("Admin ID to remove: ");
            String adminId = scanner.nextLine().trim();
            managerService.removeAdmin(manager, adminId);
            System.out.println(DONE_PREFIX + "Admin removed.");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void assignTicketTypes(User manager) {
        try {
            System.out.print("Admin ID: ");
            String adminId = scanner.nextLine().trim();
            try {
                managerService.getAdminById(manager, adminId);
            } catch (LibraryException e) {
                System.out.println(FAILED_PREFIX + e.getMessage());
                scanner.nextLine();
                return;
            }
            System.out.println("Types: " + Arrays.toString(TicketType.values()));
            System.out.println("Enter comma separated types (empty = handle ALL):");
            String input = scanner.nextLine().trim();
            List<TicketType> types = parseTicketTypes(input);
            managerService.assignTicketTypes(manager, adminId, types);
            System.out.println(DONE_PREFIX + "Ticket types updated.");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private List<TicketType> parseTicketTypes(String input) {
        List<TicketType> types = new ArrayList<>();
        if (input.isEmpty()) {
            return types;
        }
        for (String segment : input.split(",")) {
            try {
                types.add(TicketType.valueOf(segment.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                System.out.println("Unknown type ignored: " + segment.trim());
            }
        }
        return types;
    }

    private void displayUserManagement(User manager) {
        String menu = GREEN
                + "1. List/Filter Users  \n2. Edit User  \n3. Activate/Disable  \n4. Recover Password  \n5. Back"
                + RESET;
        runMenu("User Management", menu, "5", buildUserManagementActions(manager));
    }

    private Map<String, Runnable> buildUserManagementActions(User manager) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> listUsers(manager));
        actions.put("2", () -> editUser(manager));
        actions.put("3", () -> toggleUserActive(manager));
        actions.put("4", () -> recoverPassword(manager));
        return actions;
    }

    private void listUsers(User manager) {
        try {
            System.out.print("Username filter (Enter to skip): ");
            String username = scanner.nextLine().trim();
            System.out.print("ID filter (Enter to skip): ");
            String id = scanner.nextLine().trim();
            System.out.print("Role filter [GUEST/STUDENT/PROFESSOR/ADMIN/MANAGER] (Enter to skip): ");
            String roleStr = scanner.nextLine().trim();
            Role role = parseRole(roleStr);
            UserFilter filter = new UserFilter(
                    username.isEmpty() ? null : username,
                    id.isEmpty() ? null : id,
                    role);
            List<User> users = managerService.listUsers(manager, filter);
            List<String> lines = new ArrayList<>();
            for (User user : users) {
                lines.add(user.getSummary());
            }
            itemUI.displayPagedList(lines, "Users (" + users.size() + " found)");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
            scanner.nextLine();
        }
    }

    private Role parseRole(String roleStr) {
        if (roleStr.isEmpty()) {
            return null;
        }
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown role — skipping role filter.");
            return null;
        }
    }

    private void editUser(User manager) {
        System.out.print("User ID to edit: ");
        String userId = scanner.nextLine().trim();
        try {
            User target = managerService.getUserById(manager, userId);
            if (target.getRole() == Role.ADMIN) {
                System.out.println(FAILED_PREFIX + "Admins must be edited from the Admin Management menu.");
                scanner.nextLine();
                return;
            }
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
            scanner.nextLine();
            return;
        }
        editUserById(manager, userId);
    }

    private void editUserById(User manager, String userId) {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            printBanner("Edit User: " + userId, 50);
            System.out.println(GREEN + "1. Username  \n2. Email  \n3. Phone  \n4. Back" + RESET);
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals("4")) {
                running = false;
            } else {
                applyEdit(manager, userId, choice);
            }
        }
    }

    private void applyEdit(User manager, String userId, String choice) {
        String field = switch (choice) {
            case "1" -> "username";
            case "2" -> "email";
            case "3" -> "phone";
            default -> null;
        };
        if (field == null) {
            return;
        }
        System.out.print("New value: ");
        String value = scanner.nextLine().trim();
        try {
            managerService.editUserField(manager, userId, field, value);
            System.out.println(DONE_PREFIX + field + " updated.");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void toggleUserActive(User manager) {
        try {
            System.out.print("User ID: ");
            String userId = scanner.nextLine().trim();
            try {
                managerService.getUserById(manager, userId);
            } catch (LibraryException e) {
                System.out.println(FAILED_PREFIX + e.getMessage());
                scanner.nextLine();
                return;
            }
            System.out.print("[a]ctivate or [d]isable? ");
            String action = scanner.nextLine().trim().toLowerCase();
            if (!action.equals("a") && !action.equals("d")) {
                System.out.println("Invalid choice.");
                scanner.nextLine();
                return;
            }
            boolean active = action.equals("a");
            managerService.setUserActive(manager, userId, active);
            System.out.println(DONE_PREFIX + "Account " + (active ? "activated" : "disabled") + ".");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void recoverPassword(User manager) {
        try {
            System.out.print("User ID: ");
            String userId = scanner.nextLine().trim();
            managerService.getUserById(manager, userId);

            System.out.print("New password: ");
            String newPassword = scanner.nextLine();
            managerService.recoverPassword(manager, userId, newPassword);
            System.out.println(DONE_PREFIX + "Password recovered.");
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        }
        scanner.nextLine();
    }

    private void displayPolicySettings(User manager) {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            printBanner("Policy Settings", 50);
            try {
                LibraryPolicy policy = managerService.getPolicy(manager);
                printPolicy(policy);
                System.out.println(GREEN + "\n1. Edit Policy  2. Back" + RESET);
                System.out.print("> ");
                String choice = scanner.nextLine();
                if (choice.equals("2")) {
                    running = false;
                } else if (choice.equals("1")) {
                    editPolicy(manager, policy);
                }
            } catch (LibraryException e) {
                System.out.println(FAILED_PREFIX + e.getMessage());
                scanner.nextLine();
                running = false;
            }
        }
    }

    private void printPolicy(LibraryPolicy policy) {
        System.out.println(CYAN + "  Borrow Days   ==="
                + STUDENT_LBL + policy.getStudentMaxBorrowDays()
                + PROFESSOR_LBL + policy.getProfessorMaxBorrowDays()
                + GUEST_LBL + policy.getGuestMaxBorrowDays() + RESET);
        System.out.println(CYAN + "  Borrow Limits ==="
                + STUDENT_LBL + policy.getStudentMaxBorrowLimit()
                + PROFESSOR_LBL + policy.getProfessorMaxBorrowLimit()
                + GUEST_LBL + policy.getGuestMaxBorrowLimit() + RESET);
        System.out.println(CYAN + "  Fine rate: $" + policy.getFineRatePerDay() + "/day" + RESET);
        System.out.println(CYAN + "  Reservations  ==="
                + STUDENT_LBL + policy.getStudentMaxReservations()
                + PROFESSOR_LBL + policy.getProfessorMaxReservations()
                + GUEST_LBL + policy.getGuestMaxReservations()
                + "  MaxDays:" + policy.getMaxReserveDays() + RESET);
    }

    private void editPolicy(User manager, LibraryPolicy cur) {
        System.out.println("Press Enter to keep current value.");
        try {
            LibraryPolicy upd = new LibraryPolicy();
            upd.setStudentMaxBorrowDays(promptInt("Student borrow days [" + cur.getStudentMaxBorrowDays() + "]",
                    cur.getStudentMaxBorrowDays()));
            upd.setProfessorMaxBorrowDays(promptInt("Professor borrow days [" + cur.getProfessorMaxBorrowDays() + "]",
                    cur.getProfessorMaxBorrowDays()));
            upd.setGuestMaxBorrowDays(
                    promptInt("Guest borrow days [" + cur.getGuestMaxBorrowDays() + "]", cur.getGuestMaxBorrowDays()));
            upd.setStudentMaxBorrowLimit(promptInt("Student limit [" + cur.getStudentMaxBorrowLimit() + "]",
                    cur.getStudentMaxBorrowLimit()));
            upd.setProfessorMaxBorrowLimit(promptInt("Professor limit [" + cur.getProfessorMaxBorrowLimit() + "]",
                    cur.getProfessorMaxBorrowLimit()));
            upd.setGuestMaxBorrowLimit(
                    promptInt("Guest limit [" + cur.getGuestMaxBorrowLimit() + "]", cur.getGuestMaxBorrowLimit()));
            upd.setFineRatePerDay(
                    promptDouble("Fine rate/day [" + cur.getFineRatePerDay() + "]", cur.getFineRatePerDay()));
            upd.setStudentMaxReservations(promptInt("Student reservations [" + cur.getStudentMaxReservations() + "]",
                    cur.getStudentMaxReservations()));
            upd.setProfessorMaxReservations(
                    promptInt("Professor reservations [" + cur.getProfessorMaxReservations() + "]",
                            cur.getProfessorMaxReservations()));
            upd.setGuestMaxReservations(promptInt("Guest reservations [" + cur.getGuestMaxReservations() + "]",
                    cur.getGuestMaxReservations()));
            upd.setReservationPickupDays(promptInt("Pickup window (days) ["
                    + cur.getReservationPickupDays() + "]", cur.getReservationPickupDays()));
            managerService.updatePolicy(manager, upd);
            System.out.println(DONE_PREFIX + "Policy updated.");
        } catch (PolicyViolationException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        } catch (LibraryException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(FAILED_PREFIX + "Invalid number. Policy not updated.");
        }
        scanner.nextLine();
    }

    private int promptInt(String label, int defaultValue) {
        System.out.print(label + ": ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(input);
    }

    private double promptDouble(String label, double defaultValue) {
        System.out.print(label + ": ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        return Double.parseDouble(input);
    }

    private void printBanner(String text, int width) {
        System.out.println(CYAN + "╔" + ConsoleUtils.repeat('═', width) + "╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + YELLOW
                + ConsoleUtils.centerText(text, width) + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚" + ConsoleUtils.repeat('═', width) + "╝" + RESET);
    }
}