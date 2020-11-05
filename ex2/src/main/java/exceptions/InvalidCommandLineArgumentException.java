package exceptions;

public class InvalidCommandLineArgumentException extends RuntimeException {

    public InvalidCommandLineArgumentException(String message) {
        super(InvalidCommandLineArgumentException.class.getName() + "\n" + message);
    }
}
