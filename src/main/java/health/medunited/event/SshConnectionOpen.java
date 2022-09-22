package health.medunited.event;

public class SshConnectionOpen {

    //this must contain the public key of the client
    private String publicKey;

    public SshConnectionOpen() {
    }

    public SshConnectionOpen(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
