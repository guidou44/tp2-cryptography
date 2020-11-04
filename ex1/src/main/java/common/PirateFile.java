package common;

import exceptions.NoPirateParametersException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Classe qui encapsule les opérations de fichier à faire avec le fichier pirate.txt
 * Cette classe s'occupe d'écrire et d'extraire les paramètres pour l'encryption.
 * */
public class PirateFile {

  private static final String ALGORITHM_NAME = "AES";
  private static final String EXTENSIONS_IDENTIFIER = "PirateExt";
  private static final String KEY_IDENTIFIER = "PirateKey";
  private static final String IV_IDENTIFIER = "PirateIv";
  private static final String SEPARATOR = ",";
  private static final String EXTENSIONS_SEPARATOR = "Ø";

  private String pirateFilePath;   //chemain vers le fichier pirate.txt

  public PirateFile(String filePath) {
    this.pirateFilePath = filePath;
  }

  public void initializePirateFile() throws IOException {
    File file = new File(pirateFilePath);
    if (!file.createNewFile()) { //si le fichier existe pas, on le créer. Sinon on vide son contenue.
      PrintWriter pw = new PrintWriter(pirateFilePath);
      pw.close();
    }
  }

  public void saveFileExtensions(List<String> extensions) throws IOException {
    //préparation pour écrire dans le fichier pirate.txt
    BufferedWriter pirateFileWriter = new BufferedWriter(new FileWriter(pirateFilePath, true));
    String persistedExtensions = EXTENSIONS_IDENTIFIER + SEPARATOR + String.join(EXTENSIONS_SEPARATOR, extensions) + System.lineSeparator();
    pirateFileWriter.append(persistedExtensions); //ajout au fichier existant
    pirateFileWriter.close();
  }

  public List<String> readFileExtensions() throws FileNotFoundException {
    String allExtensionsConcat = readParameterInternal(EXTENSIONS_IDENTIFIER);
    return Arrays.asList(allExtensionsConcat.split(EXTENSIONS_SEPARATOR));
  }

  public void savePirateKeyAndIv(SecretKey key, IvParameterSpec ivParameterSpec)
      throws IOException {
    //préparation pour écrire dans le fichier pirate.txt
    BufferedWriter pirateFileWriter = new BufferedWriter(new FileWriter(pirateFilePath, true));
    String encodedKey = KEY_IDENTIFIER + SEPARATOR + Base64.getEncoder().encodeToString(key.getEncoded()) + System.lineSeparator(); //conversion de la clé en string, avec son identifiant comme prefix
    String encodedIv = IV_IDENTIFIER + SEPARATOR + Base64.getEncoder().encodeToString(ivParameterSpec.getIV()) + System.lineSeparator();//conversion du IV en string, avec son identifiant comme prefix
    pirateFileWriter.append(encodedKey); //ajout au fichier existant
    pirateFileWriter.append(encodedIv);//ajout au fichier existant
    pirateFileWriter.close();
  }

  /**
   * Permet de lire la clé et le iv dans le fichier pirate.txt
   * le contenus du fichier pirate va comme suite:
   * ligne 1 : Les types de fichiers supportés, séparaés par des virgules
   * ligne 2: la clé
   * ligne 3: le Iv
   *
   * il n'y aura jamais plus de ligne que cela
   */
  public Map<SecretKey, IvParameterSpec> readPirateKeyAndIv()
      throws IOException {

    String keyFromFile = readParameterInternal(KEY_IDENTIFIER);
    String ivyFromFile = readParameterInternal(IV_IDENTIFIER);

    SecretKey secretKey = reconstructSecretKey(keyFromFile);
    IvParameterSpec secretIv = reconstructIv(ivyFromFile);

    return Collections.singletonMap(secretKey, secretIv);
  }

  private String readParameterInternal(String parameterIdentifier) throws FileNotFoundException {
    //préparation pour lire le fichier pirate.txt
    File pirateFile = new File(pirateFilePath);
    if (!pirateFile.exists())
      throw new NoPirateParametersException("pirate.txt does not exist");
    Scanner fileScanner = new Scanner(pirateFile);

    while (fileScanner.hasNextLine()) {
      String lineContent = fileScanner.nextLine();
      if (lineContent.startsWith(parameterIdentifier)) {
        return lineContent.split(SEPARATOR, 2)[1];
      }
    }
    throw new NoPirateParametersException("parameter <" + parameterIdentifier + "> could not be found in pirate.txt");
  }

  /**
   * Reconstruit la clef à partir du string qui a été lue du fichier pirate.txt
   *
   * */
  private SecretKey reconstructSecretKey(String key) {
    byte[] decodedKey = Base64.getDecoder().decode(key); // decode la clé encodé en base64
    return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM_NAME);// reconstruire la clef avec SecretKeySpec
  }

  /**
   * Reconstruit le IV à partir du string qui a été lue du fichier pirate.txt
   *
   * */
  private IvParameterSpec reconstructIv(String iv) {
    byte[] decodedIv = Base64.getDecoder().decode(iv); // decode le IV encodé en base64
    return new IvParameterSpec(decodedIv);// reconstruire le IV
  }
}
