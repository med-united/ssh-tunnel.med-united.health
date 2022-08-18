package health.medunited.resource;

import health.medunited.model.PublicKeyResponse;
import health.medunited.service.PublicKeyService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/publicKey")
public class PublicKeyResource {

    private static Logger log = Logger.getLogger(PublicKeyResource.class.getName());

    @Inject
    PublicKeyService publicKeyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicKey(@QueryParam("user") String user) {
        String key = publicKeyService.findKeyInAuthorizedKeysFor(user);
        if(key != null) {
            PublicKeyResponse response = new PublicKeyResponse(key);
            log.info("Public key fetched");
            return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(404, "Key for user not found").build();
        }

    }

}
