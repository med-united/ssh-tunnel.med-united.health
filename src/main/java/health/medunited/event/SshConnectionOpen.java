package health.medunited.event;

import org.apache.sshd.common.session.Session;

public class SshConnectionOpen {

    //this must contain the public key of the client
    private String publicKey;

    private Session session;

    private String username;

    private int port;

    private String hostname;

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SshConnectionOpen(Session session) {
        this.session = session;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

}
