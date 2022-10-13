package health.medunited.artemis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.sshd.common.session.Session;

import health.medunited.event.SshConnectionClosed;
import health.medunited.event.SshConnectionOpen;


@ApplicationScoped
public class PrescriptionConsumerManager {

    private static final Logger log = Logger.getLogger(PrescriptionConsumerManager.class.getName());

    ExecutorService threadPool = Executors.newCachedThreadPool();

    Map<Session,Future<?>> openSSHConnection2PrescriptionConsumers = new ConcurrentHashMap<>();

    @Inject
    Provider<CancelablePrescriptionConsumer> cancelablePrescriptionConsumerProvider;

    void onSshConnectionOpen(@ObservesAsync SshConnectionOpen ev) {
        try {
            log.info("Session opened: "+ev.getSession());
            CancelablePrescriptionConsumer consumer = cancelablePrescriptionConsumerProvider.get();
            consumer.setPublicKeyFingerprint(ev.getPublicKey());
            Map<String, Object> connectionParameter = consumer.getConnectionParameter();
            connectionParameter.put("user", ev.getUsername().split("__")[0]);
            connectionParameter.put("password", ev.getUsername().split("__")[1]);
            connectionParameter.put("port", ev.getPort());
            connectionParameter.put("hostname", ev.getHostname());
            openSSHConnection2PrescriptionConsumers.put(ev.getSession(), threadPool.submit(new FutureTask<>(consumer)));
        } catch(Exception ex) {
            log.log(Level.SEVERE, "Could not submit prescription consumer", ex);
        }
    }

    void onSshConnectionClosed(@ObservesAsync SshConnectionClosed ev) {
        log.info("Session closed: "+ev.getSession());
        Future<?> consumer = openSSHConnection2PrescriptionConsumers.get(ev.getSession());
        consumer.cancel(true);
        openSSHConnection2PrescriptionConsumers.remove(ev.getSession());
    }
}
