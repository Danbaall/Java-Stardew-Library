package ir.ac.kntu.exceptions;

public class AccountDisabledException extends LibraryException {
    public AccountDisabledException(String username) {
        super("Account '" + username + "' is disabled. Contact the manager for assistance.");
    }
}
