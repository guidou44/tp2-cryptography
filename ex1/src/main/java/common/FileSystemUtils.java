package common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;

public class FileSystemUtils {

  private static final String CURRENT_DIRECTORY_PROPERTY = "user.dir";

  /**
   * Retourne le répertoire de travail pour l'encryption. Si le 'directoryParameter' spécifié est valide,
   * le chemain de ce répertoire est retourné. Sinon le répertoire courant est retourné.
   * */
  public static File workDirectory(String directoryParameter) {

    String currentPath = System.getProperty(CURRENT_DIRECTORY_PROPERTY);
    if (directoryParameter == null)
      return new File(currentPath);

    File specifiedFile = new File(directoryParameter);
    if (!specifiedFile.isDirectory()) {
      System.out.println("Specified directory is not a directory. Using current directory instead.");
      return new File(currentPath);
    }

    return specifiedFile;
  }

  /**
   * Retourne tous les  fichiers qu'il faut encrypter ou décrypter
   * */
  public static List<File> allFilesToManage(File workDirectory, List<String> validExtensions) {

    ArrayList<File> filesToManage = new ArrayList<>();
    List<File> directories = allSubDirectories(workDirectory);
    directories.add(workDirectory); //liste de tous les répertoires existant à partir de la racine, racine inclusivement

    for (File directory : directories) {
      //aller chercher tous les sous fichier dans le répertoire
      File[] files = Objects.requireNonNull(directory.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return !f.isDirectory();
        }
      })); //cette liste est immutable

      for (File file : files)  { //on itère sur tous les fichers dans le répertoires qui ne sont pas des répertoires
        String extension = FilenameUtils.getExtension(file.getPath()); //extraction de son extension
        if (validExtensions.contains(extension)) {
          filesToManage.add(file); //si l'extension est valide, on l'ajoute aux fichiers à encrypter/décrypter
        }
      }
    }

    return filesToManage;
  }

  public static String getFileContent(File targetFile, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(targetFile.toPath());
    return new String(encoded, encoding);
  }

  public static void overwriteFileContent(File targetFile, String newContent)
      throws IOException {
    FileOutputStream fos = new FileOutputStream(targetFile, false);
    fos.write(Base64.getDecoder().decode(newContent.getBytes()));
  }

  /**
   * Méthode récursive pour aller chercher tous les sous-répertoire existant dans le répertoire spécifé
   * */
  private static List<File> allSubDirectories(File file) {
    //aller chercher tous les sous répertoires 1 niveau plus bas
    List<File> subDirectories = Arrays.asList(Objects.requireNonNull(file.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory();
      }
    }))); //cette liste est immutable

    subDirectories = new ArrayList<File>(subDirectories); //on transforme la liste pour pouvoir la modifier
    List<File> deepSubDirectories = new ArrayList<File>(); //tous les sous-répertoires plus profond que 1 niveau
    for(File subDirectory : subDirectories) {
      deepSubDirectories.addAll(allSubDirectories(subDirectory)); //appel récursif
    }
    subDirectories.addAll(deepSubDirectories); //ajout à la liste des sous répertoires
    return subDirectories;
  }
}
