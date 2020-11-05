package credentials;

public class Credential {

    private int id;
    private String url;
    private String user;
    private String password;

    public Credential(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String modifiedUser) {
        this.user = modifiedUser;
    }

    public void setPassword(String modifiedPassword) {
        this.password = modifiedPassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
