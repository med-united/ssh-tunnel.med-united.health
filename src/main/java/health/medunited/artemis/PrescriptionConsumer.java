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

@ApplicationScoped
public class PrescriptionConsumer implements Runnable{

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    private volatile Object prescription;

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
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("Prescriptions"));
            while (true) {
                Message message = consumer.receive();
                String practiceManagementTranslation = message.getObjectProperty("practiceManagementTranslation").toString();
                String receiverPublicKeyFingerprint = message.getObjectProperty("receiverPublicKeyFingerprint").toString();
                if (message instanceof BytesMessage) {
                    String fhirBundle = getFhirBundleFromBytesMessage((BytesMessage) message);
                    PrescriptionRequest prescriptionRequest = new PrescriptionRequest(practiceManagementTranslation, receiverPublicKeyFingerprint, fhirBundle);
                    System.out.println("Prescription request content -----");
                    System.out.println(prescriptionRequest.getPracticeManagementTranslation());
                    System.out.println(prescriptionRequest.getReceiverPublicKeyFingerprint());
                    System.out.println(prescriptionRequest.getFhirBundle());
                }
                else {
                    return;
                }
            }
        } catch (JMSException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getFhirBundleFromBytesMessage(BytesMessage message) throws JMSException {
        byte[] byteData = new byte[(int) message.getBodyLength()];
        message.readBytes(byteData);
        message.reset();
        return new String(byteData);
    }


}
