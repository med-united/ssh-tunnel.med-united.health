package health.medunited.artemis;

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
            System.out.println("I'M HERE");
            JMSConsumer consumer = context.createConsumer(context.createQueue("Prescriptions"));
            while (true) {
                Message message = consumer.receive();
                if (message == null) return;
                prescription = message.getBody(Object.class);
                System.out.println(prescription);
            }
        } catch (JMSException e) {
            System.out.println(e.getMessage());
        }
    }

}
