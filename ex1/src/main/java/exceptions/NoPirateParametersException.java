package exceptions;

/**
 * Exception pour tout ce qui attrait au fichier pirate.txt
 * */
public class NoPirateParametersException extends RuntimeException {

  public NoPirateParametersException(String message) {
    super(NoPirateParametersException.class.getName() + ":\n" + message);
  }
}
