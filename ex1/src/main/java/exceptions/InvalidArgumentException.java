package exceptions;

public class InvalidArgumentException extends RuntimeException {

  public InvalidArgumentException(String message) {
    super(InvalidArgumentException.class.getName() + ":\n" + message);
  }
}
