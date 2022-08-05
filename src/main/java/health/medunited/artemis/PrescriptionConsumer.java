package health.medunited.artemis;

import health.medunited.model.PrescriptionRequest;
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
                    t2MedConnector.createPrescriptionFromBundle(prescription.getFhirBundle());
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
