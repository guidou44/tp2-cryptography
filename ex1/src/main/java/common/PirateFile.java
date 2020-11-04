package common;

import exceptions.NoPirateParametersException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Collections;
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

  public void savePirateParameters(SecretKey key, IvParameterSpec ivParameterSpec)
      throws IOException {
    //préparation pour écrire dans le fichier pirate.txt
    BufferedWriter pirateFileWriter = new BufferedWriter(new FileWriter(pirateFilePath, true));
    String encodedKey = KEY_IDENTIFIER + SEPARATOR + Base64.getEncoder().encodeToString(key.getEncoded()); //conversion de la clé en string, avec son identifiant comme prefix
    String encodedIv = IV_IDENTIFIER + SEPARATOR + Base64.getEncoder().encodeToString(ivParameterSpec.getIV());//conversion du IV en string, avec son identifiant comme prefix
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
  public Map<SecretKey, IvParameterSpec> readPirateParameters()
      throws IOException {
    //préparation pour lire le fichier pirate.txt
    File pirateFile = new File(pirateFilePath);
    if (!pirateFile.exists())
      throw new NoPirateParametersException("pirate.txt does not exist");

    Scanner fileScanner = new Scanner(pirateFile);

    String keyFromFile = null;
    String ivyFromFile = null;
    while (fileScanner.hasNextLine()) {
      String lineContent = fileScanner.nextLine();
      if (lineContent.startsWith(KEY_IDENTIFIER)) {
        keyFromFile = lineContent.split(SEPARATOR, 2)[1];
      } else if (lineContent.startsWith(IV_IDENTIFIER)) {
        ivyFromFile = lineContent.split(SEPARATOR, 2)[1];
      }
    }

    if (keyFromFile == null || ivyFromFile == null)
      throw new NoPirateParametersException("Key or iv could wer not found in pirate file.");

    SecretKey secretKey = reconstructSecretKey(keyFromFile);
    IvParameterSpec secretIv = reconstructIv(ivyFromFile);

    return Collections.singletonMap(secretKey, secretIv);
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
