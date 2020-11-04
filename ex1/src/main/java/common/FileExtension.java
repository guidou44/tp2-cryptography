package common;

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

  public String toRawExtension() {
    return ext;
  }
}
