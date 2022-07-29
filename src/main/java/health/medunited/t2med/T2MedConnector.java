package health.medunited.t2med;

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

    public T2MedConnector(){

    }
}
