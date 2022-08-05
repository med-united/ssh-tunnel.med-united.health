package health.medunited.client;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderFactory.class)
public interface T2MedClient {

    @GET
    @Path("/aps/rest/benutzer/login/authenticate")
    Response login();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/benutzer/verwalten/find")
    Response getDoctorRole();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/praxis/patient/liste/pagefilter")
    Response filterPatients();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/praxis/behandlungsfaelle/faellefuerpatientinkrementell")
    Response getCase();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/praxis/praxisstruktur/kontextauswaehlen/arztrollenbehandlungorte")
    Response getPlace();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/verordnung/rezept/ausstellen/amdb/page")
    Response searchMedicationByPzn();

    //TODO: finish the configuration of the request (pass correct body/params)
    @POST
    @Path("/aps/rest/verordnung/rezept/ausstellen/saveerezepte")
    Response createAndSavePrescription();

}
