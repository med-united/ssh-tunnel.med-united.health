package health.medunited.service;

import health.medunited.event.SSHClientPortForwardEvent;
import health.medunited.event.SshConnectionClosed;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.sshd.common.forward.DefaultForwarderFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionDisconnectHandler;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.session.helpers.TimeoutIndicator;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.apache.sshd.server.shell.ProcessShellFactory;

@ApplicationScoped
@Startup
public class SSHService {

    private static Logger log = Logger.getLogger(SSHService.class.getName());

    public static final int PORT = 22;

//    @Inject
//    SSHTunnelManager sSHTunnelManager;

    @Inject
    SSHManager sshManager;

    @Inject
    Event<SSHClientPortForwardEvent> eventSSHClientPortForwardEvent;

    @Inject
    Event<SshConnectionClosed> sshConnectionClosedEvent;

    @PostConstruct
    public void init() throws IOException {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(PORT);
        sshServer.setHost("0.0.0.0");
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setPublickeyAuthenticator((username, key, session) -> sshManager.prepareKeyForStorage(key));
        sshServer.setForwardingFilter(new AcceptAllForwardingFilter() {
            @Override
            protected boolean checkAcceptance(String request, Session session, SshdSocketAddress target) {
                eventSSHClientPortForwardEvent.fireAsync(new SSHClientPortForwardEvent(target.getHostName(), target.getPort()));
                return super.checkAcceptance(request, session, target);
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
        try {
            log.info("Starting SSH server on port: " + PORT);
            sshServer.start();
            //after starting, check when it is stopped
            sshServer.addSessionListener(new SessionListener() {
                @Override
                public void sessionDisconnect(Session session, int reason, String msg, String language, boolean initiator) {
                    sshConnectionClosedEvent.fireAsync(new SshConnectionClosed());
                    SessionListener.super.sessionDisconnect(session, reason, msg, language, initiator);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
