package health.medunited.client;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Base64;

@ApplicationScoped
public class AuthorizationHeaderFactory implements ClientHeadersFactory {

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                 MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        String user = System.getProperty("t2med.username");
        String password = System.getProperty("t2med.password");
        String originalInput = user + ":" + password;
        String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
        result.add("Authorization", "Basic " + encodedString);
        return result;
    }
}
