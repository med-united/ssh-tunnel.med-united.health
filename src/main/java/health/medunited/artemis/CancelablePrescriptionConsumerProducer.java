package health.medunited.artemis;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class CancelablePrescriptionConsumerProducer {
    @Produces
    public CancelablePrescriptionConsumer produce() {
        return new CancelablePrescriptionConsumer();
    }
}
