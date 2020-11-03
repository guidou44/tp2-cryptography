package cipher;

import exceptions.NoPirateParametersException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
* Cette classe est un "wrapper" pour la décryption et l'encryption. elle encapsule les détails de
* l'implémentation.
* */
public class AesCbc128Cipher {

  private static final String ALGORITHM_NAME = "AES";
  private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
  private static final String KEY_IDENTIFIER = "PirateKey";
  private static final String IV_IDENTIFIER = "PirateIv";
  private static final String SEPARATOR = ",";

  private String pirateFilePath;   //chemain vers le fichier pirate.txt

  public AesCbc128Cipher(String pirateFilePath) {
    this.pirateFilePath = pirateFilePath;
  }

  public String encrypt(String strToEncrypt) {

    try {
      Cipher cipher = Cipher.getInstance(CIPHER_NAME);//créer une instance d'un algorithme AES avec un mode CBC et un padding de type PKCS5
      SecretKey secretKey = generateSecretKey(); //on génère la clef aléatoire
      IvParameterSpec iv = generateInitialisationVector(); //on génère le vecteur IV
      savePirateParameters(secretKey, iv); //on sauvegarde la clef et IV dans le fichier pirate.txt pour la décryption futur
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);//initialier la clé, iv et indiquer qu'il s'agit d'un chifrement
      byte[] encrypted =cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));//chiffrer le message

      return Base64.getEncoder().encodeToString(encrypted);//encoder le résultat en base64

    } catch (Exception e) {
      //en cas d'erreur, afficher un message et retourner la chaine nulle comme résultat
      System.out.println("[ERROR] Error while encrypting: " + e.toString());
      return null;
    }
  }

  public String decrypt(String strToDecrypt) {
    try
    {

      Cipher cipher = Cipher.getInstance(CIPHER_NAME);//créer une instance d'un algorithme AES avec un mode CBC et un padding de type PKCS5
      Map<SecretKey, IvParameterSpec> persistedParameters = readPirateParameters(); //on va lire la dernière clef et le dernier iv dans le fichier pirate.txt

      //extraction des infos du dictionnaire
      Map.Entry<SecretKey,IvParameterSpec> entry = persistedParameters.entrySet().iterator().next();
      SecretKey secretKey = entry.getKey();
      IvParameterSpec iv = entry.getValue();

      cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);//initialier la clé, iv et indiquer qu'il s'agit d'un déchifrement
      byte[] encryptedBytes = Base64.getDecoder().decode(strToDecrypt);//Décoder le message chiffré de sa forme base64
      byte[] original = cipher.doFinal(encryptedBytes);//déchifrer le message
      return new String(original);
    }
    catch (Exception e) {
      //en cas d'erreur, afficher un message et retourner la chaine nulle comme résultat
      System.out.println("[ERROR] Error while decrypting: " + e.toString());
      return null;
    }
  }

  private SecretKey generateSecretKey() throws NoSuchAlgorithmException {

    KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_NAME);    //créer une instance d'un générateur de clés AES
    keyGen.init(128);    //initialiser la taille de la clé
    return keyGen.generateKey(); //générer aléatoirement une clé AES de 128 bits
  }

  private IvParameterSpec generateInitialisationVector() {

    SecureRandom random = new SecureRandom();    //Créer une instance d'un générateur aléatoire sécuritaire
    byte[] iv0 = random.generateSeed(16);    //générer iv aléatoirement de 16 octes
    return new IvParameterSpec(iv0);
  }

  private void savePirateParameters(SecretKey key, IvParameterSpec ivParameterSpec)
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
  private Map<SecretKey, IvParameterSpec> readPirateParameters()
      throws IOException {
    //préparation pour lire le fichier pirate.txt
    File pirateFile = new File(pirateFilePath);
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
