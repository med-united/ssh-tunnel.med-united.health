package health.medunited.client;

import javax.json.JsonObject;
import javax.ws.rs.*;

import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface T2MedClient {

    @GET
    @Path("/aps/rest/benutzer/login/authenticate")
    JsonObject login(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/benutzer/verwalten/find")
    JsonObject getDoctorRole(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, JsonObject findVerwalt);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/praxis/patient/liste/pagefilter")
    JsonObject filterPatients(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, JsonObject searchPatient);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/praxis/behandlungsfaelle/faellefuerpatientinkrementell")
    JsonObject getCase(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, JsonObject searchCase);

    @GET   
    @Consumes("application/json")
    @Path("/aps/rest/praxis/praxisstruktur/kontextauswaehlen/arztrollenbehandlungorte")
    JsonObject getCaseLocation(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/verordnung/rezept/ausstellen/amdb/page")
    JsonObject searchMedication(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, JsonObject amdbSearchQuery);

    @POST
    @Consumes("application/json")
    @Path("/aps/rest/verordnung/rezept/ausstellen/saveerezepte")
    JsonObject createAndSavePrescription(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization, JsonObject eRezept);

}
