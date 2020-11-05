import cipher.AesCtr128Cipher;
import credentials.CredentialPrinter;
import common.Mode;
import credentials.Credential;
import credentials.CredentialBank;
import exceptions.InvalidCommandLineArgumentException;
import org.apache.commons.cli.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class Main {

    private static final String HIDDEN = "******";

    private static String externalPassword;
    private static String url;
    private static String user;
    private static String password;
    private static int id;
    private static boolean decryptUser;
    private static boolean decryptPassword;
    private static Mode mode;


    public static void main(String[] args) {

        try {
            extractCommandLineArguments(args);
            CredentialBank credentialBank = new CredentialBank();
            AesCtr128Cipher cipher = new AesCtr128Cipher();

            if (externalPassword == null || externalPassword.isEmpty())
                throw new InvalidCommandLineArgumentException("Extarnal password needs to be specified and not empy.");

            switch (mode) {
                case Add:
                    addMode(credentialBank, cipher);
                    break;
                case List:
                    listMode(credentialBank);
                    break;
                case Decrypt:
                    decryptMode(credentialBank, cipher);
                    break;
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void listMode(CredentialBank credentialBank) throws IOException {
        List<Credential> allCredentials = credentialBank.getAllCredentials();
        for (Credential cred : allCredentials) {
            cred.setUser(HIDDEN);
            cred.setPassword(HIDDEN);
        }
        CredentialPrinter.printCredentials(allCredentials);
    }

    private static void addMode(CredentialBank credentialBank, AesCtr128Cipher cipher)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (url == null || url.isEmpty() || user == null || user.isEmpty() || password == null || password.isEmpty())
            throw new InvalidCommandLineArgumentException("Not all required arguments were specified for add mode");
        Credential credential = new Credential(url, user, password);//creation du nouveau credential
        cipher.encrypt(credential, externalPassword); //encryption
        credentialBank.addCredential(credential);//ajout au fichier json
    }

    private static void decryptMode(CredentialBank credentialBank, AesCtr128Cipher cipher) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (id <= 0)
            throw new InvalidCommandLineArgumentException("The Id passed needs to be greater that 0.");
        List<Credential> allCredentials = credentialBank.getAllCredentials();
        Optional<Credential> credOfInterest = allCredentials.stream().filter(c -> c.getId() == id).findFirst();
        if (credOfInterest.isPresent()) {
            Credential credential = credOfInterest.get();
            if (decryptUser) {
                String clearUser = cipher.decrypt(credential.getUser(), password);
                credential.setUser(clearUser);
            } else {
                credential.setPassword(HIDDEN);
            }
            if (decryptPassword) {
                String clearPassword = cipher.decrypt(credential.getPassword(), password);
                credential.setPassword(clearPassword);
            } else {
                credential.setPassword(HIDDEN);
            }

            CredentialPrinter.printCredential(credential, true);
        } else {
            System.out.println("no credentials exists with id : " + id);
        }
    }

    private static void extractCommandLineArguments(String[] args) throws ParseException {
        Options options = new Options();

        setPossibleOptions(options);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("utility-name", options);
            throw e;
        }

        String decryptMode = cmd.getOptionValue("d");
        String addMode = cmd.getOptionValue("a");
        String listMode = cmd.getOptionValue("l");

        if (decryptMode != null) {
            extractDecryptModeParameters(cmd, decryptMode);
        } else if (addMode != null) {
            extractAddModeParameters(cmd, addMode);
        } else if (listMode != null) {
            extractListModeParameters(listMode);
        } else {
            throw new InvalidCommandLineArgumentException("no possible option mode was passed to ex2");
        }
     }

    private static void setPossibleOptions(Options options) {
        Option addArg = new Option("a", true,"add credentials, specify your secret password.");
        addArg.setRequired(false);
        options.addOption(addArg);

        Option urlArg = new Option("url", true, "website to save credentials for.");
        urlArg.setRequired(false);
        options.addOption(urlArg);

        Option userArg = new Option("user", true, "user for the credentials to add.");
        userArg.setOptionalArg(true);
        userArg.setRequired(false);
        options.addOption(userArg);

        Option passwordArg = new Option("pwd", true, "password for the credentials to add.");
        passwordArg.setOptionalArg(true);
        passwordArg.setRequired(false);
        options.addOption(passwordArg);

        Option listArg = new Option("l", true, "list mode to list all credentials, specify your secret password.");
        listArg.setRequired(false);
        options.addOption(listArg);

        Option decryptArg = new Option("d", true, "show specific credential with specified id " +
                "with decrypted elements if needed, specify your secret password.");
        decryptArg.setRequired(false);
        options.addOption(decryptArg);

        Option idArg = new Option("i", true, "id of the credential you want to access with -d option.");
        idArg.setRequired(false);
        options.addOption(idArg);
    }

    private static void extractAddModeParameters(CommandLine cmd, String addMode) {
        mode = Mode.Add;
        externalPassword = addMode;
        url = cmd.getOptionValue("url");
        user = cmd.getOptionValue("user");
        password = cmd.getOptionValue("pwd");
    }

    private static void extractDecryptModeParameters(CommandLine cmd, String decryptMode) {
        mode = Mode.Decrypt;
        externalPassword = decryptMode;
        decryptUser = cmd.hasOption("user");
        decryptPassword = cmd.hasOption("pwd");
        String idString = cmd.getOptionValue("i");
        if (idString == null)
            throw new InvalidCommandLineArgumentException("No Id was passed for -d mode");
        id = Integer.parseInt(idString);
    }

    private static void extractListModeParameters(String listMode) {
        mode = Mode.List;
        externalPassword = listMode;
    }
}

