package exceptions;

/**
 * Exception pour tout ce qui attrait aux arguments du programme
 * */
public class InvalidArgumentException extends RuntimeException {

  public InvalidArgumentException(String message) {
    super(InvalidArgumentException.class.getName() + ":\n" + message);
  }
}
