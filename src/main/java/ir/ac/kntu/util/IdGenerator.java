package ir.ac.kntu.util;

import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.enums.Role;
import java.security.SecureRandom;

public class IdGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static String generateUserId(Role role) {
        String prefix = switch (role) {
            case PROFESSOR -> "fac";
            case STUDENT -> "stu";
            case GUEST -> "gst";
            case ADMIN -> "adm";
            case MANAGER -> "mgr";
        };
        return prefix + "-" + generateRandomString(6);
    }

    public static String generateItemId(ItemType type) {
        String prefix = switch (type) {
            case BOOK -> "bk";
            case MAGAZINE -> "mg";
            case EBOOK -> "eb";
            case AUDIOBOOK -> "ab";
        };
        return prefix + "-" + generateRandomString(8);
    }

    public static String generateTransactionId() {
        return "txn-" + generateRandomString(8);
    }

    public static String generateTicketId() {
        return "tkt-" + generateRandomString(8);
    }

    public static String generateBorrowId() {
        return "brw-" + generateRandomString(8);
    }

    public static String generateReservationId() {
        return "res-" + generateRandomString(8);
    }

    public static String generateNotificationId() {
        return "ntf-" + generateRandomString(8);
    }
}
