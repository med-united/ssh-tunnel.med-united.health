package health.medunited.artemis;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jms.*;
import javax.jms.IllegalStateException;

import org.hl7.fhir.r4.model.Bundle;

import health.medunited.isynet.IsynetMSQLConnector;
import health.medunited.model.PrescriptionRequest;
import health.medunited.service.BundleParser;
import health.medunited.t2med.T2MedConnector;

@Dependent
public class CancelablePrescriptionConsumer implements Callable<Void> {

    private static final Logger log = Logger.getLogger(CancelablePrescriptionConsumer.class.getName());

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    IsynetMSQLConnector isynetMSQLConnector;

    @Inject
    T2MedConnector t2MedConnector;

    private static final String PVS_HEADER = "practiceManagementTranslation";

    private static final String FINGERPRINT_HEADER = "receiverPublicKeyFingerprint";

    private static final String PRACTITIONER = "practitioner";
    private static final String PATIENT = "patient";
    private static final String MEDICATIONSTATEMENT = "medicationStatement";
    private static final String PHARMACY = "organization";

    private String publicKeyFingerprint;

    private Map<String, Object> connectionParameter = new HashMap<>(); 

    public CancelablePrescriptionConsumer() {

    }

    public String getPublicKeyFingerprint() {
        return this.publicKeyFingerprint;
    }

    public void setPublicKeyFingerprint(String publicKeyFingerprint) {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }

    public Map<String, Object> getConnectionParameter() {
        return connectionParameter;
    }

    @Override
    public Void call() throws Exception {

        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue("Prescriptions");
            try (JMSConsumer consumer = context.createConsumer(queue, "receiverPublicKeyFingerprint = '" + publicKeyFingerprint + "'")) {
                while (true) {
                    try {
                        Message message = consumer.receive();
                        if (message == null) {
                            continue;
                        }
                        if (message.propertyExists(FINGERPRINT_HEADER) && message.propertyExists(PVS_HEADER)) {
                            String practiceManagement = message.getObjectProperty(PVS_HEADER).toString();
                            String fhirBundle = getFhirBundleFromBytesMessage((BytesMessage) message);
                            PrescriptionRequest prescription = new PrescriptionRequest(practiceManagement, publicKeyFingerprint, fhirBundle);

                            Bundle parsedBundle = BundleParser.parseBundle(prescription.getFhirBundle());

                            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                            log.info("[ PRACTITIONER ]" + " first name: " + BundleParser.getFirstName(PRACTITIONER, parsedBundle) +
                                    " // last name: " + BundleParser.getLastName(PRACTITIONER, parsedBundle) +
                                    " // LANR: " + BundleParser.getLanr(PRACTITIONER, parsedBundle) +
                                    " // street: " + BundleParser.getStreet(PRACTITIONER, parsedBundle) +
                                    " // house number: " + BundleParser.getHouseNumber(PRACTITIONER, parsedBundle) +
                                    " // city: " + BundleParser.getCity(PRACTITIONER, parsedBundle) +
                                    " // postal code: " + BundleParser.getPostalCode(PRACTITIONER, parsedBundle) +
                                    " // e-mail: " + BundleParser.getEmail(PRACTITIONER, parsedBundle) +
                                    " // phone: " + BundleParser.getPhone(PRACTITIONER, parsedBundle) +
                                    " // fax: " + BundleParser.getFax(PRACTITIONER, parsedBundle) +
                                    " // modality: " + BundleParser.getModality(PRACTITIONER, parsedBundle));

                            log.info("[ PATIENT ]" + " first name: " + BundleParser.getFirstName(PATIENT, parsedBundle) +
                                    " // last name: " + BundleParser.getLastName(PATIENT, parsedBundle) +
                                    " // street: " + BundleParser.getStreet(PATIENT, parsedBundle) +
                                    " // house number: " + BundleParser.getHouseNumber(PATIENT, parsedBundle) +
                                    " // city: " + BundleParser.getCity(PATIENT, parsedBundle) +
                                    " // postal code: " + BundleParser.getPostalCode(PATIENT, parsedBundle) +
                                    " // gender: " + BundleParser.getGender(PATIENT, parsedBundle) +
                                    " // birthDate: " + BundleParser.getBirthDate(PATIENT, parsedBundle));

                            log.info("[ MEDICATION STATEMENT ]" + " medication name: " + BundleParser.getMedicationName(MEDICATIONSTATEMENT, parsedBundle) +
                                    " // PZN: " + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) +
                                    " // dosage: " + BundleParser.getDosage(MEDICATIONSTATEMENT, parsedBundle));

                            log.info("[ PHARMACY ]" + " name: " + BundleParser.getName(PHARMACY, parsedBundle) +
                                    " // street: " + BundleParser.getStreet(PHARMACY, parsedBundle) +
                                    " // house number: " + BundleParser.getHouseNumber(PHARMACY, parsedBundle) +
                                    " // city: " + BundleParser.getCity(PHARMACY, parsedBundle) +
                                    " // postal code: " + BundleParser.getPostalCode(PHARMACY, parsedBundle) +
                                    " // phone: " + BundleParser.getPhone(PHARMACY, parsedBundle) +
                                    " // email: " + BundleParser.getEmail(PHARMACY, parsedBundle) + "\n");

                            if (Objects.equals(message.getStringProperty(PVS_HEADER), "isynet")) {

                                isynetMSQLConnector.insertToIsynet(parsedBundle, connectionParameter);

                            } else if (Objects.equals(message.getStringProperty(PVS_HEADER), "t2med")) {
                                setT2MedCredentialsIntoSystemProperties(connectionParameter);
                                t2MedConnector.createPrescriptionFromBundle(parsedBundle,
                                        "https://" + connectionParameter.get("hostname").toString() + ":" + connectionParameter.get("port").toString());
                            }

                        } else {
                            log.info("Invalid content");
                        }
                    } catch (Exception e) {
                        if(e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause().getCause() instanceof InterruptedException) {
                            log.info("Prescription Consumer Interrupted e.g. by SSH Connection close. Ending.");
                            consumer.close();
                            break;
                        } else if (e.getCause() instanceof IllegalStateRuntimeException || e.getCause() instanceof IllegalStateException) {
                            log.info("Prescription Consumer Closed e.g.: consumer was manually closed in Artemis console. Ending.");
                            break;
                        } else {
                            log.log(Level.SEVERE, "Problem will processing message", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getFhirBundleFromBytesMessage(BytesMessage message) throws JMSException {
        byte[] byteData = new byte[(int) message.getBodyLength()];
        message.readBytes(byteData);
        message.reset();
        return new String(byteData);
    }

    private void setT2MedCredentialsIntoSystemProperties(Map<String, Object> connectionParameter) {
        System.setProperty("t2med.username", connectionParameter.get("user").toString());
        System.setProperty("t2med.password", connectionParameter.get("password").toString());
        System.setProperty("quarkus.rest-client.T2MedClient.url",
                "https://"
                        + connectionParameter.get("hostname").toString()
                        + ":"
                        + connectionParameter.get("port").toString());

    }

}
