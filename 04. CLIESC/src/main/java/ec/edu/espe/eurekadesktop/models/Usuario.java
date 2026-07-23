package ec.edu.espe.eurekadesktop.models;

public class Usuario {
    private String username;
    private String token;
    private Backend backend;

    public Usuario() {}

    public Usuario(String username, String token, Backend backend) {
        this.username = username;
        this.token = token;
        this.backend = backend;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }
}