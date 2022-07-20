package health.medunited;

import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;

import java.nio.file.Path;

public final class Constants {

    private Constants() {

    }

    public static final Path AUTHORIZED_KEYS_FILE = AuthorizedKeysAuthenticator.getDefaultAuthorizedKeysFile();
}
