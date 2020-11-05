package credentials;

import credentials.Credential;

import java.util.List;

public class CredentialPrinter {

    private static final String LINE_HEADER = "line";
    private static final String PASSWORD_HEADER = "pwd";
    private static final String URL_HEADER = "url";
    private static final String USER_HEADER = "user";
    private static final String COLUMN_SPACE = "          ";

    public static void printCredentials(List<Credential> credentials) {
        printHeaders();

        for (Credential cred : credentials) {
            printCredential(cred, false);
        }
    }

    public static void printCredential(Credential credential, boolean withHeaders) {
        if (withHeaders)
            printHeaders();
        System.out.println(credential.getId() + COLUMN_SPACE + credential.getUrl() +
                COLUMN_SPACE + credential.getUser() + COLUMN_SPACE + credential.getPassword());
    }

    private static void printHeaders() {
        System.out.println(LINE_HEADER + COLUMN_SPACE + URL_HEADER + COLUMN_SPACE +
                USER_HEADER + COLUMN_SPACE + PASSWORD_HEADER);
    }
}
