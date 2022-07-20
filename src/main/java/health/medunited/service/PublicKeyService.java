package health.medunited.service;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static health.medunited.Constants.AUTHORIZED_KEYS_FILE;

@ApplicationScoped
public class PublicKeyService {

    public String findKeyInAuthorizedKeysFor(String user) {

        AtomicReference<String> foundLine = new AtomicReference<>("");

        try (Stream<String> lines = Files.lines(AUTHORIZED_KEYS_FILE)) {
            lines.filter(line -> line.contains(user))
                    .forEach(foundLine::set);
            return splitLine(foundLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String splitLine(AtomicReference<String> foundKey) {
        if (!foundKey.get().isEmpty()) {
            return foundKey.get().split(" ")[1];
        } else {
            return null;
        }
    }
}
