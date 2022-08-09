package health.medunited.client;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderFactory.class)
public interface T2MedClient {

    @GET
    @Path("/aps/rest/benutzer/login/authenticate")
    JsonObject login();

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/benutzer/verwalten/find")
    JsonObject getDoctorRole(JsonObject findVerwalt);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/praxis/patient/liste/pagefilter")
    JsonObject filterPatients(JsonObject searchPatient);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/praxis/behandlungsfaelle/faellefuerpatientinkrementell")
    JsonObject getCase(JsonObject searchCase);

    @GET   
    @Consumes("application/json")
    @Path("/aps/rest/praxis/praxisstruktur/kontextauswaehlen/arztrollenbehandlungorte")
    JsonObject getCaseLocation();

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/verordnung/rezept/ausstellen/amdb/page")
    JsonObject searchMedication(JsonObject amdbSearchQuery);

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/verordnung/rezept/ausstellen/saveerezepte")
    JsonObject createAndSavePrescription();

}
