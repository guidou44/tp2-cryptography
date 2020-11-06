package exceptions;

public class NotInversibleInZnException extends RuntimeException {
    public NotInversibleInZnException(String message) {
        super(NotInversibleInZnException.class.getName() + System.lineSeparator() + message);
    }
}
