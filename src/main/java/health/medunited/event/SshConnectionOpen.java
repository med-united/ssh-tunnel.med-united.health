package health.medunited.event;

import org.apache.sshd.common.session.Session;

public class SshConnectionOpen {

    //this must contain the public key of the client
    private String publicKey;

    private Session session;

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
