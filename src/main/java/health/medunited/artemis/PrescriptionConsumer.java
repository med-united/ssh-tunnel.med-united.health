package health.medunited.artemis;

import health.medunited.model.PrescriptionRequest;
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

    private static Logger log = Logger.getLogger(PrescriptionConsumer.class.getName());

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

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
             JMSConsumer consumer = context.createConsumer(context.createQueue("Prescriptions"))) {
            while (true) {
                Message message = consumer.receive();
                if(message == null) return;
                if (message.propertyExists("receiverPublicKeyFingerprint") && message.propertyExists("practiceManagementTranslation")) {
                    String publicKey = message.getObjectProperty("receiverPublicKeyFingerprint").toString();
                    String practiceManagement = message.getObjectProperty("practiceManagementTranslation").toString();
                    prescription = new PrescriptionRequest(practiceManagement, publicKey, message.getBody(String.class));
                    log.info("Content of Bundle: " + prescription.getFhirBundle());
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
