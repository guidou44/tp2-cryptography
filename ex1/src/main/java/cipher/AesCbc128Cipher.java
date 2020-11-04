package cipher;

import common.PirateFile;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
* Cette classe est un "wrapper" pour la décryption et l'encryption. elle encapsule les détails de
* l'implémentation.
* */
public class AesCbc128Cipher {

  private static final String ALGORITHM_NAME = "AES";
  private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";

  private PirateFile pirateFile; //permet de d'accéder à pirate.txt
  private Charset charset; //charset pour encodage
  private SecretKey secretKey = null;
  private IvParameterSpec iv = null;

  public AesCbc128Cipher(PirateFile pirateFile, Charset charset) {
    this.pirateFile = pirateFile;
    this.charset = charset;


  }

  public String encrypt(String strToEncrypt) {

    try {
      if (this.secretKey == null && this.iv == null) {
        this.secretKey = generateSecretKey(); //on génère la clef aléatoire
        this.iv = generateInitialisationVector(); //on génère le vecteur IV
        pirateFile.savePirateKeyAndIv(secretKey, iv); //on sauvegarde la clef et IV dans le fichier pirate.txt pour la décryption futur
      }
      Cipher cipher = Cipher.getInstance(CIPHER_NAME);//créer une instance d'un algorithme AES avec un mode CBC et un padding de type PKCS5
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);//initialier la clé, iv et indiquer qu'il s'agit d'un chifrement
      byte[] encrypted =cipher.doFinal(strToEncrypt.getBytes(charset));//chiffrer le message

      return Base64.getEncoder().encodeToString(encrypted);//encoder le résultat en base64

    } catch (Exception e) {
      //en cas d'erreur, afficher un message et retourner la chaine nulle comme résultat
      System.out.println("[ERROR] Error while encrypting: " + e.toString());
      return null;
    }
  }

  public String decrypt(String strToDecrypt) {
    try {
      if (this.secretKey == null && this.iv == null) {
        Map<SecretKey, IvParameterSpec> persistedParameters = pirateFile
            .readPirateKeyAndIv(); //on va lire la dernière clef et le dernier iv dans le fichier pirate.txt
        //extraction des infos du dictionnaire
        Map.Entry<SecretKey, IvParameterSpec> entry = persistedParameters.entrySet().iterator()
            .next();
        this.secretKey = entry.getKey();
        this.iv = entry.getValue();
      }
      Cipher cipher = Cipher.getInstance(
          CIPHER_NAME);//créer une instance d'un algorithme AES avec un mode CBC et un padding de type PKCS5
      cipher.init(Cipher.DECRYPT_MODE, secretKey,
          iv);//initialier la clé, iv et indiquer qu'il s'agit d'un déchifrement
      byte[] encryptedBytes = Base64.getDecoder()
          .decode(strToDecrypt);//Décoder le message chiffré de sa forme base64
      byte[] original = cipher.doFinal(encryptedBytes);//déchifrer le message
      return new String(original);
    } catch (Exception e) {
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



}
