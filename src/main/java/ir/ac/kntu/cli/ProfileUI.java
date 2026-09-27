package ir.ac.kntu.cli;

import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.exceptions.AccountDisabledException;
import ir.ac.kntu.services.AuthService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.util.ServiceResult;
import java.util.Scanner;

public class ProfileUI {

    private static final String DONE_PREFIX = "done, ";
    private static final String FAILED_PREFIX = "failed, ";

    private final AuthService authService;
    private final UserService userService;
    private final Scanner scanner;

    public ProfileUI(
        AuthService authService,
        UserService userService,
        Scanner scanner
    ) {
        this.authService = authService;
        this.userService = userService;
        this.scanner = scanner;
    }

    public void displayProfile(User user) {
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println(user.toString());
            System.out.println("1. Update email");
            System.out.println("2. Update phone number");
            System.out.println("3. Update password");
            System.out.println("4. Return");
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals("1")) {
                handleUpdateEmail(user);
            } else if (choice.equals("2")) {
                handleUpdatePhone(user);
            } else if (choice.equals("3")) {
                handleUpdatePassword(user);
            } else if (choice.equals("4")) {
                return;
            }
        }
    }

    public User displayRegistration() {
        System.out.println("========= REGISTER =========");
        System.out.println("1. Guest");
        System.out.println("2. Student");
        System.out.println("3. Professor");
        System.out.print("> ");
        String role = scanner.nextLine();
        String[] fields = readBaseFields();

        String code = authService.generateGateCode();
        System.out.println("Your verification code: " + code);
        System.out.print("Enter the code to proceed: ");
        if (!scanner.nextLine().equals(code)) {
            System.out.println(
                FAILED_PREFIX + "Invalid code. Registration cancelled."
            );
            scanner.nextLine();
            return null;
        }

        try {
            ServiceResult<User> res = registerWithRole(role, fields);
            if (res != null) {
                String prefix = res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX;
                System.out.println(prefix + res.getMessage());
                scanner.nextLine();
                return res.getData();
            }
        } catch (AccountDisabledException e) {
            System.out.println(FAILED_PREFIX + e.getMessage());
            scanner.nextLine();
        }
        return null;
    }

    private String[] readBaseFields() {
        System.out.print("Username: ");
        String uname = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        return new String[] { uname, email, phone, pass };
    }

    private ServiceResult<User> registerWithRole(String role, String[] fields) {
        String uname = fields[0],
            email = fields[1],
            phone = fields[2],
            pass = fields[3];
        if (role.equals("1")) {
            return authService.registerGuest(uname, email, phone, pass);
        } else if (role.equals("2")) {
            System.out.print("Student ID: ");
            String sid = scanner.nextLine();
            return authService.registerStudent(
                uname,
                email,
                pass,
                phone + "::" + sid
            );
        } else if (role.equals("3")) {
            System.out.print("Department: ");
            String dept = scanner.nextLine();
            return authService.registerProfessor(
                uname,
                email,
                pass,
                phone + "::" + dept
            );
        }
        return null;
    }

    private void handleUpdateEmail(User user) {
        System.out.print("New Email (or Enter to skip): ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            ServiceResult<Void> res = userService.updateEmail(user, email);
            System.out.println(
                (res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) +
                    res.getMessage()
            );
            scanner.nextLine();
        }
    }

    private void handleUpdatePhone(User user) {
        System.out.print("New Phone (or Enter to skip): ");
        String phoneNumber = scanner.nextLine();
        if (!phoneNumber.isEmpty()) {
            ServiceResult<Void> res = userService.updatePhone(
                user,
                phoneNumber
            );
            System.out.println(
                (res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) +
                    res.getMessage()
            );
            scanner.nextLine();
        }
    }

    private void handleUpdatePassword(User user) {
        System.out.print("Old Password: ");
        String old = scanner.nextLine();
        System.out.print("New Password: ");
        String newp = scanner.nextLine();
        if (!old.isEmpty() && !newp.isEmpty()) {
            ServiceResult<Void> res = userService.updatePassword(
                user,
                old,
                newp
            );
            System.out.println(
                (res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) +
                    res.getMessage()
            );
            scanner.nextLine();
        }
    }
}
