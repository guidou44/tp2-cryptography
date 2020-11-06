package exceptions;

public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(InvalidParameterException.class.getName() + System.lineSeparator() + message);
    }
}
