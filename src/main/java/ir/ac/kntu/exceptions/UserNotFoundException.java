package ir.ac.kntu.exceptions;

public class UserNotFoundException extends LibraryException {
    public UserNotFoundException(String identifier) {
        super("User not found " + identifier);
    }
}
