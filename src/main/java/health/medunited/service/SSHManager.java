package health.medunited.service;

import static health.medunited.Constants.AUTHORIZED_KEYS_FILE;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.bouncycastle.util.encoders.Base64;

@ApplicationScoped
public class SSHManager {

    private static Logger log = Logger.getLogger(SSHManager.class.getName());

    private static final String SSH_RSA = "ssh-rsa";

    public boolean prepareKeyForStorage(PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        String publicKeyEncoded = encodePublicKey(rsaPublicKey);
        storeKeyInAuthorizedKeysFile(publicKeyEncoded);
        return true;
    }

    static String encodePublicKey(RSAPublicKey rsaPublicKey) {
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteOs);
        try {
            dos.writeInt(SSH_RSA.getBytes().length);
            dos.write(SSH_RSA.getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(byteOs.toByteArray()));
    }

    private void storeKeyInAuthorizedKeysFile(String encodedKey) {
        try (Stream<String> lines = Files.lines(AUTHORIZED_KEYS_FILE)) {
            if (lines.noneMatch(l -> l.contains(encodedKey))) {
                String content = "\n" + SSH_RSA + " " + encodedKey + " " + LocalDateTime.now();
                Files.write(AUTHORIZED_KEYS_FILE, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
