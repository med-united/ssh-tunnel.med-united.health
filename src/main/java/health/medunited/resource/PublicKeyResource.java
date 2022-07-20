package health.medunited.resource;

import health.medunited.model.PublicKeyResponse;
import health.medunited.service.PublicKeyService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/publicKey")
public class PublicKeyResource {

    @Inject
    PublicKeyService publicKeyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicKey(String user) {
        String key = publicKeyService.findKeyInAuthorizedKeysFor(user);
        if(key != null) {
            PublicKeyResponse response = new PublicKeyResponse(key);
            return Response.ok().entity(response).build();
        } else {
            return Response.status(404, "Key for user not found").build();
        }

    }

}
