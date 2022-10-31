package health.medunited.client;

public class AuthorizationService {

    private AuthorizationService() {
        throw new IllegalStateException("Utility class");
    }

    public static String getAuthorizationHeader() {
        String user = System.getProperty("t2med.username");
        String password = System.getProperty("t2med.password");
        String originalInput = user + ":" + password;
        String encodedString = java.util.Base64.getEncoder().encodeToString(originalInput.getBytes());
        return "Basic " + encodedString;
    }
}
