package health.medunited.artemis;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.medunited.isynet.IsynetMSQLConnector;
import health.medunited.model.BundleStructure;
import health.medunited.model.MedicationStatement;
import health.medunited.model.Patient;
import health.medunited.model.Pharmacy;
import health.medunited.model.Practitioner;
import health.medunited.model.PrescriptionRequest;
import health.medunited.service.JsonExtractor;
import health.medunited.t2med.T2MedConnector;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class PrescriptionConsumer implements Runnable {

    private static final Logger log = Logger.getLogger(PrescriptionConsumer.class.getName());

    private static final String PVS_HEADER = "practiceManagementTranslation";

    private static final String FINGERPRINT_HEADER = "receiverPublicKeyFingerprint";

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
    IsynetMSQLConnector isynetMSQLConnector;

    @Inject
    T2MedConnector t2MedConnector;

    @Inject
    JsonExtractor jsonExtractor;

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue("Prescriptions");
            int IDValue = 0;
            while (true) {
                try (JMSConsumer consumer = context.createConsumer(queue)) {
                    Message message = consumer.receive();
                    IDValue += 1;
                    if (message == null) return;
                    if (message.propertyExists(FINGERPRINT_HEADER) && message.propertyExists(PVS_HEADER)) {
                        String publicKey = message.getObjectProperty(FINGERPRINT_HEADER).toString();
                        String practiceManagement = message.getObjectProperty(PVS_HEADER).toString();
                        String fhirBundle = getFhirBundleFromBytesMessage((BytesMessage) message);
                        prescription = new PrescriptionRequest(practiceManagement, publicKey, fhirBundle);
                        log.info("Content of Bundle: " + prescription.getFhirBundle());

                        if (Objects.equals(message.getStringProperty(PVS_HEADER), "isynet")) {

                            JSONObject json = new JSONObject(prescription.getFhirBundle());
    //                        JSON bundle for testing
    //                        JSONObject json = new JSONObject("{\"resourceType\": \"Bundle\",\t\"id\": \"\", \"meta\": { \"lastUpdated\": \"\", \"profile\": [\t\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\"\t]},\t\"identifier\": { \"system\": \"https://gematik.de/fhir/NamingSystem/PrescriptionID\", \"value\": \"\"}, \"type\": \"document\",\"timestamp\": \"2022-08-01T19:41:55.47Z\",\"entry\" : [{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Practitioner\",\"id\":\"1429\",\"meta\":{\"versionId\":\"11\",\"lastUpdated\":\"2022-07-29T13:46:49.326+01:00\",\"source\":\"#IpFF8lgXn51VD1hL\"},\"extension\":[{\"url\":null,\"valueString\":\"isynet\"}],\"name\":[{\"use\":\"official\",\"family\":\"Hoffmann\",\"given\":[\"Emma\"]}],\"telecom\":[{\"value\":\"beatriz.correia@incentergy.de\"},{\"value\":\"111111111\"},{\"value\":\"30 123456789\"}],\"address\":[{\"use\":\"home\",\"line\":[\"Alt-Moabit\"],\"city\":\"Berlin\",\"postalCode\":\"10555\"}]}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Patient\",\"id\":\"2061\",\"meta\":{\"versionId\":\"1\",\"lastUpdated\":\"2022-06-27T18:13:32.099+01:00\",\"source\":\"#zLn26PfiLgUR54w2\"},\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Max <b>MUSTERMANN </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Address</td><td><span>Orange street </span><br/><span>Berlin </span></td></tr><tr><td>Date of birth</td><td><span>01 January 1995</span></td></tr></tbody></table></div>\"},\"name\":[{\"use\":\"official\",\"family\":\"Tomaszewska\",\"given\":[\"Agnieszka\"]}],\"gender\":\"female\",\"birthDate\":\"1975-03-03\",\"address\":[{\"use\":\"home\",\"line\":[\"Orange street\"],\"city\":\"Berlin\",\"postalCode\":\"12345\"}],\"generalPractitioner\":[{\"reference\":\"Practitioner/1429\"}],\"managingOrganization\":{\"reference\":\"Organization/155\"}}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"MedicationStatement\",\"id\":\"2164\",\"meta\":{\"versionId\":\"1\",\"lastUpdated\":\"2022-06-29T14:16:58.222+01:00\",\"source\":\"#8vvT7jy5XQ8aQNJZ\"},\"identifier\":[{\"value\":\"10166894\"}],\"medicationCodeableConcept\":{\"text\":\"FLOXAL Augentropfen\"},\"subject\":{\"reference\":\"Patient/2061\"},\"informationSource\":{\"reference\":\"Practitioner/1429\"},\"derivedFrom\":[{\"reference\":\"Organization/555\"}],\"dosage\":[{\"text\":\"1-0-1-0\"}]}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Organization\",\"id\":\"555\",\"meta\":{\"versionId\":\"3\",\"lastUpdated\":\"2022-06-29T12:24:06.088+01:00\",\"source\":\"#q7CatK72HI4CXj8u\"},\"name\":\"Kaiser Apotheke\",\"telecom\":[{\"value\":\"+49 300000000\"},{\"value\":\"beatriz.correia@incentergy.de\"}],\"address\":[{\"line\":[\"Bergmannstraße\"],\"city\":\"Berlin\",\"postalCode\":\"10999\"}]}}]}");

                            JSONObject practitionerJSON = (JSONObject) ((JSONArray) json.get("entry")).get(0);
                            JSONObject patientJSON = (JSONObject) ((JSONArray) json.get("entry")).get(1);
                            JSONObject medicationStatementJSON = (JSONObject) ((JSONArray) json.get("entry")).get(2);
                            JSONObject pharmacyJSON = (JSONObject) ((JSONArray) json.get("entry")).get(3);

                            Practitioner practitioner = jsonExtractor.parsePractitioner(practitionerJSON);
                            log.info("[ PRACTITIONER ]" + " first name: " + practitioner.getFirstName() + " // last name: " + practitioner.getLastName() + " // street: " + practitioner.getStreet() + " // house number: " + practitioner.getHouseNumber() + " // city: " + practitioner.getCity() + " // postal code: " + practitioner.getPostalCode() + " // e-mail: " + practitioner.getEmail() + " // phone: " + practitioner.getPhone() + " // fax: " + practitioner.getFax() + " // modality: " + practitioner.getModality());

                            Patient patient = jsonExtractor.parsePatient(patientJSON);
                            log.info("[ PATIENT ]" + " first name: " + patient.getFirstName() + " // last name: " + patient.getLastName() + " // street: " + patient.getStreet() + " // house number: " + patient.getHouseNumber() + " // city: " + patient.getCity() + " // postal code: " + patient.getPostalCode() + " // gender: " + patient.getGender() + " // birthDate: " + patient.getBirthDate());

                            MedicationStatement medicationStatement = jsonExtractor.parseMedicationStatement(medicationStatementJSON);
                            log.info("[ MEDICATION STATEMENT ]" + " medication name: " + medicationStatement.getMedicationName() + " // PZN: " + medicationStatement.getPZN() + " // dosage: " + medicationStatement.getDosage());

                            Pharmacy pharmacy = jsonExtractor.parsePharmacy(pharmacyJSON);
                            log.info("[ PHARMACY ]" + " name: " + pharmacy.getName() + " // street: " + pharmacy.getStreet() + " // house number: " + pharmacy.getHouseNumber() + " // city: " + pharmacy.getCity() + " // postal code: " + pharmacy.getPostalCode() + " // phone: " + pharmacy.getPhone() + " // email: " + pharmacy.getEmail() + "\n");

                            BundleStructure bundleStructure = new BundleStructure(practitioner, patient, medicationStatement, pharmacy);
                            isynetMSQLConnector.insertToIsynet(bundleStructure, IDValue);

                        } else if (Objects.equals(message.getStringProperty(PVS_HEADER), "t2med")) {

                            FhirContext ctx = FhirContext.forR4();

                            IParser parser = ctx.newJsonParser();

                            org.hl7.fhir.r4.model.Bundle parsedBundle = parser.parseResource(org.hl7.fhir.r4.model.Bundle.class, prescription.getFhirBundle());

                            t2MedConnector.createPrescriptionFromBundle(parsedBundle);
                        }

                    } else {
                        log.info("Invalid content");
                    }
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFhirBundleFromBytesMessage(BytesMessage message) throws JMSException {
        byte[] byteData = new byte[(int) message.getBodyLength()];
        message.readBytes(byteData);
        message.reset();
        return new String(byteData);
    }

}
