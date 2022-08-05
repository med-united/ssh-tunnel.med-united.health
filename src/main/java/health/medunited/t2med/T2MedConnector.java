package health.medunited.t2med;

import health.medunited.client.T2MedClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.springframework.stereotype.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    public void createPrescriptionFromBundle(String prescription) {
        System.out.println("---------------------->>>>>>>" + t2MedClient.login().getStatusInfo());
        //TODO: encapsulate prescription in a Bundle with a FHIR resource parser
        //TODO: Do other calls to server and pass adequate parameters from the prescription

    }
}
