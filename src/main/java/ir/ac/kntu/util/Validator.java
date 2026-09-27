package ir.ac.kntu.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

@SuppressWarnings("PMD.AtLeastOneConstructor")
public class Validator {

    private static final String LOCAL = "[A-Za-z0-9]+([._+\\-][A-Za-z0-9]+)*";
    private static final String DOMAIN_LABEL = "[A-Za-z0-9]([A-Za-z0-9\\-]*[A-Za-z0-9])?";
    private static final String TLD = "[A-Za-z]{2,6}";
    private static final String DOMAIN = DOMAIN_LABEL + "(\\." + DOMAIN_LABEL + ")*\\." + TLD;
    private static final String EMAIL = "^" + LOCAL + "@" + DOMAIN + "$";

    private static final String PHONE1 = "^09[0-9]{9}$";
    private static final String PHONE2 = "^\\+989[0-9]{9}$";
    private static final String PHONE3 = "^00989[0-9]{9}$";

    private static final String USER_ID = "^(fac|stu|gst|adm|mgr)-[A-Z0-9]{6}$";
    private static final String ITEM_ID = "^(bk|mg|eb|ab)-[A-Z0-9]{8}$";
    private static final String URL = "https://.+";
    private static final String ISBN_13 = "(979|978)\\d{10}";
    private static final String ISSN = "^\\d{4}-\\d{3}[\\dXx]$";

    private final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private final Pattern passwordPattern = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL);
    }

    public boolean isValidPhone(String phone) {
        return phone != null
                && (phone.matches(PHONE1) || phone.matches(PHONE2) || phone.matches(PHONE3));
    }

    public boolean isValidUserId(String id) {
        return id != null && id.matches(USER_ID);
    }

    public boolean isValidItemId(String id) {
        return id != null && id.matches(ITEM_ID);
    }

    public boolean isValidUrl(String url) {
        return url != null && url.matches(URL);
    }

    public boolean isValidIsbn(String isbn) {
        return isbn != null && isbn.matches(ISBN_13);
    }

    public boolean isValidIssn(String issn) {
        return issn != null && issn.matches(ISSN);
    }

    public boolean isValidPublishYear(String publishYear) {
        int currentYear = LocalDate.now().getYear();
        int year = Integer.parseInt(publishYear);
        return year > 1450 && year <= currentYear;
    }

    public boolean isValidUsername(String username) {
        return username != null && usernamePattern.matcher(username).matches();
    }

    public boolean isStrongPassword(String password) {
        return password != null && passwordPattern.matcher(password).matches();
    }
}
