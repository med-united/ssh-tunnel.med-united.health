package health.medunited.service;

import health.medunited.profile.CustomTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(CustomTestProfile.class)
public class BundleParserTest {

    private final String PATIENT = "patient";
    private final String PRACTITIONER = "practitioner";

    private final InputStream fhirBundleWithAllFieldsIS = getClass().getClassLoader().getResourceAsStream("fhirBundleWithAllFields.txt");
    private final InputStream fhirBundleWithEmptyFieldsIS = getClass().getClassLoader().getResourceAsStream("fhirBundleWithEmptyFields.txt");

    private final String fhirBundleAsStringWithAllFields = IOUtils.toString(fhirBundleWithAllFieldsIS, StandardCharsets.UTF_8);
    private final String fhirBundleAsStringWithEmptyFields = IOUtils.toString(fhirBundleWithEmptyFieldsIS, StandardCharsets.UTF_8);

    private final Bundle fhirBundleWithAllFields = BundleParser.parseBundle(fhirBundleAsStringWithAllFields);
    private final Bundle fhirBundleWithEmptyFields = BundleParser.parseBundle(fhirBundleAsStringWithEmptyFields);

    public BundleParserTest() throws IOException {}

    @Test
    public void testParsedBundle() {
        assertThat(BundleParser.parseBundle(fhirBundleAsStringWithAllFields), instanceOf(Bundle.class));
        assertThat(BundleParser.parseBundle(fhirBundleAsStringWithEmptyFields), instanceOf(Bundle.class));
    }

    @Test
    public void testGetPatientFirstName() {
        assertEquals("Emma", BundleParser.getFirstName(PATIENT, this.fhirBundleWithAllFields));
    }

    @Test
    public void testGetPatientFirstNameThatIsEmpty() {
        assertEquals("", BundleParser.getFirstName(PATIENT, this.fhirBundleWithEmptyFields));
    }

    @Test
    public void testGetPractitionerFirstName() {
        assertEquals("Theresa", BundleParser.getFirstName(PRACTITIONER, this.fhirBundleWithAllFields));
    }

    @Test
    public void testGetPractitionerFirstNameThatIsEmpty() {
        assertEquals("", BundleParser.getFirstName(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    @Test
    public void testGetPatientLastName() {
        assertEquals("Schneider", BundleParser.getLastName(PATIENT, this.fhirBundleWithAllFields));
    }

    @Test
    public void testGetPractitionerLastName() throws IOException {
        assertEquals("Hoffmann", BundleParser.getLastName(PRACTITIONER, this.fhirBundleWithAllFields));
    }
}
