package health.medunited.service;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.enterprise.event.ObservesAsync;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import health.medunited.event.NoteEvent;

public class FHIRUpdater {
    private static Logger log = Logger.getLogger(FHIRUpdater.class.getSimpleName());

    Client client = ClientBuilder.newClient();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public void addNoteToFhirResource(@ObservesAsync NoteEvent noteEvent) {
        String dateString = sdf.format(noteEvent.getDate());
        String requestString = "[ {\"op\": \"add\", \"path\": \"/note\", \"value\": []},{ \"op\": \"add\", \"path\": \"/note/0\", \"value\": "+
        "{\"authorString\": \"Test\", \"time\": \""+dateString+"\", \"text\": \"Test\"} }]";

        // [{ "op": "add", "path": "/note", "value": {"authorString": "Test", "time": "2022", "text": "Test"} }]
        Response response = client.target("https://fhir.med-united.health/fhir")
            .path("/MedicationRequest").path("/"+noteEvent.getReferencedObject())
            .request("application/json")
            .header("Authorization", "Bearer "+noteEvent.getJwt())
            .method("PATCH", Entity.entity(
                requestString
            , "application/json-patch+json"));
        
            if(!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                log.warning(response.getStatus()+" "+response.readEntity(String.class)+" Request: "+requestString);
            }

    }
    
}
