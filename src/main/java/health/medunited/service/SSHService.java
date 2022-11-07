package health.medunited.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.sshd.common.forward.DefaultForwarderFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionDisconnectHandler;
import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.session.helpers.TimeoutIndicator;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

import health.medunited.event.SSHClientPortForwardEvent;
import health.medunited.event.SshConnectionClosed;
import health.medunited.event.SshConnectionOpen;
import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class SSHService {

    private static Logger log = Logger.getLogger(SSHService.class.getName());

    public static final int PORT = 22;

    @Inject
    SSHManager sshManager;

    @Inject
    Event<SSHClientPortForwardEvent> eventSSHClientPortForwardEvent;


    @Inject
    Event<SshConnectionOpen> sshConnectionOpenEvent;

    @Inject
    Event<SshConnectionClosed> sshConnectionClosedEvent;

    static Map<Session, SshConnectionOpen> session2key = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        try {
            SshServer sshServer = SshServer.setUpDefaultServer();
            sshServer.setPort(PORT);
            sshServer.setHost("0.0.0.0");
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sshServer.setPublickeyAuthenticator((username, key, session) -> {
                try {
                    SshConnectionOpen sshConnectionOpen = session2key.get(session);
                    if(sshConnectionOpen != null) {
                        sshConnectionOpen.setPublicKey(SSHManager.encodePublicKey((RSAPublicKey) key));
                        sshConnectionOpen.setUsername(username);
                    }
                    return sshManager.prepareKeyForStorage(key);
                } catch(Exception ex) {
                    log.log(Level.SEVERE, "Problem with PublickeyAuthenticator", ex);
                    return false;
                }
            });
            sshServer.setForwardingFilter(new AcceptAllForwardingFilter() {
                @Override
                protected boolean checkAcceptance(String request, Session session, SshdSocketAddress target) {
                    boolean retVal = super.checkAcceptance(request, session, target);
                    SshConnectionOpen sshConnectionOpen = session2key.get(session);
                    if(sshConnectionOpen != null) {
                        sshConnectionOpen.setPort(target.getPort());
                        sshConnectionOpen.setHostname(target.getHostName());
                        session2key.remove(session);
                        sshConnectionOpenEvent.fireAsync(sshConnectionOpen);
                    }
                    eventSSHClientPortForwardEvent.fireAsync(new SSHClientPortForwardEvent(target.getHostName(), target.getPort()));
                    return retVal;
                }
            });
            sshServer.setSessionHeartbeat(HeartbeatType.IGNORE, Duration.ofSeconds(5));
            //I want to print something when the client disconnects
            sshServer.setSessionDisconnectHandler(new SessionDisconnectHandler() {
                @Override
                public boolean handleTimeoutDisconnectReason(Session session, TimeoutIndicator timeoutStatus) {
                    log.info("Client disconnected due to timeout");
                    return false;
                }
            });
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                sshServer.setShellFactory(new ProcessShellFactory("cmd", "cmd"));
            } else {
                sshServer.setShellFactory(new ProcessShellFactory("/bin/sh", "/bin/sh", "-i", "-l"));
            }
            sshServer.setForwarderFactory(DefaultForwarderFactory.INSTANCE);

            log.info("Starting SSH server on port: " + PORT);
            sshServer.start();
            //after starting, check when it is stopped
            sshServer.addSessionListener(new SessionListener() {
                @Override
                public void sessionCreated(Session session) {
                    try {
                        session2key.put(session, new SshConnectionOpen(session));
                    } catch(Exception ex) {
                        log.log(Level.SEVERE, "Problem with sessionCreated", ex);
                    }
                }
                @Override
                public void sessionDisconnect(Session session, int reason, String msg, String language, boolean initiator) {
                    sshConnectionClosedEvent.fireAsync(new SshConnectionClosed(session));
                    SessionListener.super.sessionDisconnect(session, reason, msg, language, initiator);
                }
            });
        } catch (Exception  e) {
            log.log(Level.SEVERE, "There is a problem with SSH Server", e);
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    new FileInputStream("src/test/resources/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new SSHService().init();
        System.out.println("Press any Key to stop ...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
