package health.medunited.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

public class CustomTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() {
        return "dev";
    }
}