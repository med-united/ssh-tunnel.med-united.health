package health.medunited.client;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderFactory.class)
public interface T2MedClient {

    @GET
    @Path("/aps/rest/benutzer/login/authenticate")
    public Response login();

}
