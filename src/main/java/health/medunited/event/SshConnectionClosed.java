package health.medunited.event;

import org.apache.sshd.common.session.Session;

public class SshConnectionClosed {

    Session session;

    public SshConnectionClosed(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

}
