package exceptions;

public class NoPirateParametersException extends RuntimeException {

  public NoPirateParametersException(String message) {
    super(NoPirateParametersException.class.getName() + ":\n" + message);
  }
}
