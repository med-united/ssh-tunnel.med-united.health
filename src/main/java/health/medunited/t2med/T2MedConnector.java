package health.medunited.t2med;

import health.medunited.client.T2MedClient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;

@ApplicationScoped
public class T2MedConnector {
    // https://github.com/med-united/care.med-united.health/blob/main/webapp/resources/local/t2med.ps1
    // Server: demo.t2med.com Username: t2user, no password
    // curl --user t2user: -k https://demo.t2med.com:16567/aps/rest/benutzer/login/authenticate -v
    // install T2Med client: https://download.t2med.de/
    // create 2.0 client
    // set authentication base
    // authenticate against T2-Med system
    // extract user reference from the JSON object
    // get doctor's role
    // extract doctor's role reference from JSON
    // find patient by last name first name
    // get most recent case - handlungsfile
    // get location for treatment
    // find medication
    // extract attributes from medication
    // create and save prescription

    @Inject
    @RestClient
    T2MedClient t2MedClient;

    public void createPrescriptionFromBundle(Bundle prescription) {

        Response res = t2MedClient.login();
        JsonObject test = getJsonObject(res);
        System.out.println("DONE");
        //TODO: encapsulate prescription in a Bundle with a FHIR resource parser
        //TODO: Do other calls to server and pass adequate parameters from the prescription

    }

    private JsonObject getJsonObject(Response response) {
        String jsonString = response.readEntity(String.class);
        JsonObject jsonObject = JsonObject.EMPTY_JSON_OBJECT;

        if (StringUtils.isNotBlank(jsonString)) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
                jsonObject = jsonReader.readObject();
            }
        }

        return jsonObject;
    }
}
