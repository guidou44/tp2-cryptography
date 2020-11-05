package credentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CredentialBank {

    private static final String CURRENT_DIRECTORY_PROPERTY = "user.dir";
    private static final String CRED_BANK_FILE_NAME = "credentials.json";

    private final Gson serializer = new GsonBuilder().setPrettyPrinting().create();

    private File credentialsFile;

    public CredentialBank() throws IOException {
        initializeCredentialBank();
    }

    /**
     * Initialize la banque de credentials. Si le fichier n'existe pas, on le créer
     * */
    private void initializeCredentialBank() throws IOException {
        File file = new File(workDirectoryPath() + "/" + CRED_BANK_FILE_NAME);
        file.createNewFile(); //si le fichier en question n'existe pas déjà, on le créer
        credentialsFile = file;
    }

    /**
     * fonction qui retourne le répertoire courant, i.e. le répertoire de travail
     * */
    private static String workDirectoryPath() {
        return System.getProperty(CURRENT_DIRECTORY_PROPERTY);
    }

    /**
     * Fonction qui permet d'ajouter un credential
     * */
    public void addCredential(Credential newCredential) throws IOException {
        List<Credential> allCredentials = getAllCredentials(); //la liste des credentials déjà persistés, en ordre croissant de Id
        Optional<Credential> lastAdded = allCredentials.stream().reduce((first, second) -> second); //dernier credential ajouté

        int nextId = 1;
        if (lastAdded.isPresent()) {
            nextId = 1 + lastAdded.get().getId(); //si la liste n'est pas vide, le prochain Id est le dernier ajouté + 1
        }
        newCredential.setId(nextId); //on set le Id au nouveau credential
        allCredentials.add(newCredential); //on ajoute le credential a la collection
        try (Writer writer = new FileWriter(credentialsFile, false)) {
            serializer.toJson(allCredentials, writer); //on persiste la collection de credentials, en écrasant le ficheir actuel
        }
    }

    /**
     * Fonction qui permet de récupérer tous les credentials en ordre croissant de leur Id
     * */
    public List<Credential> getAllCredentials() throws IOException {
        //try-with-resources : il faut fermer le reader s'il y a un problème
        try (Reader reader = new FileReader(credentialsFile)) {
            Type credentialListType = new TypeToken<ArrayList<Credential>>(){}.getType(); //on veut les credentials dans une liste.
            List<Credential> credentials = serializer.fromJson(reader, credentialListType);
            credentials.sort(Comparator.comparingInt(Credential::getId)); //on veut que les credentials soient ordonnées pour faciliter l'affichage ou attribuer le prochain id.
            return credentials;
        }
    }
}
