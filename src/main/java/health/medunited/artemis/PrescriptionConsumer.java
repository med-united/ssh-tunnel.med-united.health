package health.medunited.artemis;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.medunited.model.*;
import health.medunited.service.JsonExtractor;
import health.medunited.t2med.T2MedConnector;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@ApplicationScoped
public class PrescriptionConsumer implements Runnable {

    private static final Logger log = Logger.getLogger(PrescriptionConsumer.class.getName());

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    private PrescriptionRequest prescription;

    public Object getTest() {
        return prescription;
    }

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Inject
    T2MedConnector t2MedConnector;

    @Inject
    JsonExtractor jsonExtractor;

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
             JMSConsumer consumer = context.createConsumer(context.createQueue("Prescriptions"))) {
            while (true) {
                Message message = consumer.receive();
                if (message == null) return;
                if (message.propertyExists("receiverPublicKeyFingerprint") && message.propertyExists("practiceManagementTranslation")) {
                    String publicKey = message.getObjectProperty("receiverPublicKeyFingerprint").toString();
                    String practiceManagement = message.getObjectProperty("practiceManagementTranslation").toString();
                    prescription = new PrescriptionRequest(practiceManagement, publicKey, message.getBody(String.class));
                    log.info("Content of Bundle: " + prescription.getFhirBundle());

                    FhirContext ctx = FhirContext.forR4();

                    IParser parser = ctx.newJsonParser();

                    org.hl7.fhir.r4.model.Bundle parsed = parser.parseResource(org.hl7.fhir.r4.model.Bundle.class, prescription.getFhirBundle());

                    //                    JSONObject json = new JSONObject(prescription.getFhirBundle());
//                    JSONObject practitionerJSON = (JSONObject) ((JSONArray) json.get("entry")).get(0);
//                    JSONObject patientJSON = (JSONObject) ((JSONArray) json.get("entry")).get(1);
//                    JSONObject medicationStatementJSON = (JSONObject) ((JSONArray) json.get("entry")).get(2);
//                    JSONObject pharmacyJSON = (JSONObject) ((JSONArray) json.get("entry")).get(3);
//
//                    Practitioner practitioner = parsePractitioner(practitionerJSON);
//                    log.info("[ PRACTITIONER ]" + "first name: " + practitioner.getFirstName() + "last name: " + practitioner.getLastName() + "street: " + practitioner.getStreet() + "house number: " + practitioner.getHouseNumber() + "city: " + practitioner.getCity() + "postal code: " + practitioner.getPostalCode() + "e-mail: " + practitioner.getEmail() + "phone: " + practitioner.getPhone() + "fax: " + practitioner.getFax() + "modality: " + practitioner.getModality());
//
//                    Patient patient = parsePatient(patientJSON);
//                    log.info("[ PATIENT ]" + "first name: " + patient.getFirstName() + "last name: " + patient.getLastName() + "street: " + patient.getStreet() + "house number: " + patient.getHouseNumber() + "city: " + patient.getCity() + "postal code: " + patient.getPostalCode() + "gender: " + patient.getGender() + "birthDate: " + patient.getBirthDate());
//
//                    MedicationStatement medicationStatement = parseMedicationStatement(medicationStatementJSON);
//                    log.info("[ MEDICATION STATEMENT ]" + "medication name: " + medicationStatement.getMedicationName() + "PZN: " + medicationStatement.getPZN() + "dosage: " + medicationStatement.getDosage());
//
//                    Pharmacy pharmacy = parsePharmacy(pharmacyJSON);
//                    log.info("[ PHARMACY ]" + "name: " + pharmacy.getName() + "street: " + pharmacy.getStreet() + "house number: " + pharmacy.getHouseNumber() + "city: " + pharmacy.getCity() + "postal code: " + pharmacy.getPostalCode() + "phone: " + pharmacy.getPhone() + "email: " + pharmacy.getEmail());

                    //TODO: parse Bundle before calling this method
                    //t2MedConnector.createPrescriptionFromBundle(prescription.getFhirBundle());

                } else {
                    log.info("Invalid content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: delete if not needed anymore
    private String getFhirBundleFromBytesMessage(BytesMessage message) throws JMSException {
        byte[] byteData = new byte[(int) message.getBodyLength()];
        message.readBytes(byteData);
        message.reset();
        return new String(byteData);
    }

}
