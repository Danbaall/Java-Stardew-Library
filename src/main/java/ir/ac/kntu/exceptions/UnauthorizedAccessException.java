package ir.ac.kntu.exceptions;

public class UnauthorizedAccessException extends LibraryException {
    public UnauthorizedAccessException() {
        super("You are not authorized to perform this action.");
    }

    public UnauthorizedAccessException(String detail) {
        super(detail);
    }
}
