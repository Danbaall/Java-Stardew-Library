package ir.ac.kntu.exceptions;

public class DuplicateUserException extends LibraryException {
    public DuplicateUserException(String field, String value) {
        super(field + " is already in use: " + value);
    }
}
