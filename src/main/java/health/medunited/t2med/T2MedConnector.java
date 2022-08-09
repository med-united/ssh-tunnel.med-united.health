package health.medunited.t2med;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;

import health.medunited.client.T2MedClient;

@ApplicationScoped
public class T2MedConnector {

    private static Logger log = Logger.getLogger(T2MedConnector.class.getName());
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

        JsonObject login = t2MedClient.login();
        
        log.info("Login successfully? "+login.getBoolean("successful"));
        //TODO: encapsulate prescription in a Bundle with a FHIR resource parser
        //TODO: Do other calls to server and pass adequate parameters from the prescription

        // $userReference = $response | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        // Write-Host "User reference: " $userReference

        String userReference = login.getJsonObject("benutzer").getJsonObject("benutzer").getJsonObject("ref").getJsonObject("objectId").getJsonString("id").getString();

        log.info("User reference: "+userReference);

        JsonObject findVerwalt = Json.createObjectBuilder()
            .add("benutzerRef", 
                Json.createObjectBuilder().add("objectId",
                    Json.createObjectBuilder().add("id", userReference)
                )
            ).add("findOnlyAssigned", true).build();
        
        JsonObject doctorReferenceJson = t2MedClient.getDoctorRole(findVerwalt);

        String lanr = prescription.getEntry().get(0).getResource().getChildByName("identifier").getValues().get(0).getChildByName("value").getValues().get(0).toString();
        // Select-Object -ExpandProperty "benutzerBearbeitenDTO" | Select-Object -ExpandProperty "arztrollen" | Select-Object -ExpandProperty "arztrolle" | Where-Object -Property lanr -eq -Value $lanr | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String doctorReference = ((JsonObject)doctorReferenceJson.getJsonObject("benutzerBearbeitenDTO").getJsonArray("arztrollen").stream().filter(jv -> jv instanceof JsonObject && ((JsonObject)jv).getJsonObject("arztrolle").getString("lanr").equals(lanr)).findFirst().get()).getJsonObject("arztrolle").getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Doctor reference: "+doctorReference);

        Patient patient = (Patient) prescription.getEntry().stream().filter(p -> p.getResource().getResourceType() == ResourceType.Patient).findFirst().get().getResource();

        JsonObject searchPatient = Json.createObjectBuilder().add("searchString", patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGivenAsSingleString()).build();
        
        JsonObject searchPatientJsonResponse = t2MedClient.filterPatients(searchPatient);

        // $patientReference = $response | Select-Object -ExpandProperty "patientSearchResultDTOS" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String patientReference = ((JsonObject)searchPatientJsonResponse.getJsonArray("patientSearchResultDTOS").get(0)).getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Patient reference: "+patientReference);

        JsonObject searchCase = Json.createObjectBuilder().add("objectId", Json.createObjectBuilder().add("id", patientReference)).build();

        JsonObject searchCaseJson = t2MedClient.getCase(searchCase);

        // $caseReference = $response | Select-Object -ExpandProperty "zeilenMaps" | Select-Object -ExpandProperty "AKTUELL" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String caseReference = ((JsonObject)searchCaseJson.getJsonObject("zeilenMaps").getJsonArray("AKTUELL").get(0)).getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Case Reference: "+caseReference);

        JsonObject caseLocationJson = t2MedClient.getCaseLocation();
        // $caseLocationReference = $response | Select-Object -ExpandProperty "behandlungsorte" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" -First 1 | Select-Object -ExpandProperty "id"
        String caseLocationReference = ((JsonObject)caseLocationJson.getJsonArray("behandlungsorte").get(0)).getJsonObject("ref").getJsonObject("objectId").getString("id");
        log.info("Case Location Reference: "+caseLocationReference);

        String pzn = prescription.getEntry().get(2).getResource().getChildByName("identifier").getValues().get(0).getChildByName("value").getValues().get(0).toString();

        JsonObject amdbSearch = Json.createObjectBuilder().add("amdbSearchQueries", Json.createArrayBuilder().add(Json.createObjectBuilder().add("searchtext",pzn))).build();

        JsonObject amdbResponseJson = t2MedClient.searchMedication(amdbSearch);

    }

}
