package health.medunited.model;

public class PublicKeyResponse {

    private String key;

    public PublicKeyResponse() {
    }

    public PublicKeyResponse(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
