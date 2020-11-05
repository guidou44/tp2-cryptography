package common;

/**
 * Cette enum sert à storer les extensions de fichier acceptée dans des constantes. Le fait d'utiliser un enum
 * assure une meilleur maintenabilité du code.
 * */
public enum FileExtension {
  XLS("xls"),
  DOC("doc"),
  PDF("pdf"),
  PNG("png"),
  MP3("mp3"),
  AVI("avi"),
  TXT("txt");


  private String ext;
  FileExtension(String ext) {
    this.ext = ext;
  }

  /**
   * Retourne l'extension correspondante sous forme de string.
   * */
  public String toRawExtension() {
    return ext;
  }
}
