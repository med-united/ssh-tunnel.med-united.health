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
    private final String MEDICATIONSTATEMENT = "medicationStatement";
    private final String PHARMACY = "organization";

    private final InputStream fhirBundleWithAllFieldsIS = getClass().getClassLoader().getResourceAsStream("fhirBundleWithAllFields.txt");
    private final InputStream fhirBundleWithEmptyFieldsIS = getClass().getClassLoader().getResourceAsStream("fhirBundleWithEmptyFields.txt");

    private final String fhirBundleAsStringWithAllFields = IOUtils.toString(fhirBundleWithAllFieldsIS, StandardCharsets.UTF_8);
    private final String fhirBundleAsStringWithEmptyFields = IOUtils.toString(fhirBundleWithEmptyFieldsIS, StandardCharsets.UTF_8);

    private final Bundle fhirBundleWithAllFields = BundleParser.parseBundle(fhirBundleAsStringWithAllFields);
    private final Bundle fhirBundleWithEmptyFields = BundleParser.parseBundle(fhirBundleAsStringWithEmptyFields);

    public BundleParserTest() throws IOException {}

    @Test
    void testParsedBundle() {
        assertThat(BundleParser.parseBundle(fhirBundleAsStringWithAllFields), instanceOf(Bundle.class));
        assertThat(BundleParser.parseBundle(fhirBundleAsStringWithEmptyFields), instanceOf(Bundle.class));
    }

    // getFirstName
    @Test
    void testGetPatientFirstName() {
        assertEquals("Emma", BundleParser.getFirstName(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientFirstNameThatIsEmpty() {
        assertEquals("", BundleParser.getFirstName(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPractitionerFirstName() {
        assertEquals("Theresa", BundleParser.getFirstName(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerFirstNameThatIsEmpty() {
        assertEquals("", BundleParser.getFirstName(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    // getLastName
    @Test
    void testGetPatientLastName() {
        assertEquals("Schneider", BundleParser.getLastName(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientLastNameThatIsEmpty() {
        assertEquals("", BundleParser.getLastName(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPractitionerLastName() {
        assertEquals("Hoffmann", BundleParser.getLastName(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerLastNameThatIsEmpty() {
        assertEquals("", BundleParser.getLastName(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    // getStreet
    @Test
    void testGetPatientStreet() {
        assertEquals("Orange avenue", BundleParser.getStreet(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientStreetThatIsEmpty() {
        assertEquals("", BundleParser.getStreet(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPractitionerStreet() {
        assertEquals("Blue avenue", BundleParser.getStreet(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerStreetThatIsEmpty() {
        assertEquals("", BundleParser.getStreet(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyStreet() {
        assertEquals("Bergmannstraße", BundleParser.getStreet(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyStreetThatIsEmpty() {
        assertEquals("", BundleParser.getStreet(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getHouseNumber
    @Test
    void testGetPractitionerHouseNumber() {
        assertEquals("11", BundleParser.getHouseNumber(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerHouseNumberThatIsEmpty() {
        assertEquals("", BundleParser.getHouseNumber(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPatientHouseNumber() {
        assertEquals("5", BundleParser.getHouseNumber(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientHouseNumberThatIsEmpty() {
        assertEquals("", BundleParser.getHouseNumber(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyHouseNumber() {
        assertEquals("", BundleParser.getHouseNumber(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyHouseNumberThatIsEmpty() {
        assertEquals("", BundleParser.getHouseNumber(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getCity
    @Test
    void testGetPractitionerCity() {
        assertEquals("Berlin", BundleParser.getCity(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerCityThatIsEmpty() {
        assertEquals("", BundleParser.getCity(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPatientCity() {
        assertEquals("München", BundleParser.getCity(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientCityThatIsEmpty() {
        assertEquals("", BundleParser.getCity(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyCity() {
        assertEquals("Berlin", BundleParser.getCity(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyCityThatIsEmpty() {
        assertEquals("", BundleParser.getCity(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getPostalCode
    @Test
    void testGetPractitionerPostalCode() {
        assertEquals("10115", BundleParser.getPostalCode(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerPostalCodeThatIsEmpty() {
        assertEquals("", BundleParser.getPostalCode(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPatientPostalCode() {
        assertEquals("80333", BundleParser.getPostalCode(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientPostalCodeThatIsEmpty() {
        assertEquals("", BundleParser.getPostalCode(PATIENT, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyPostalCode() {
        assertEquals("10961", BundleParser.getPostalCode(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyPostalCodeThatIsEmpty() {
        assertEquals("", BundleParser.getPostalCode(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getLanr
    @Test
    void testGetLanr() {
        assertEquals("123456601", BundleParser.getLanr(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetLanrThatIsEmpty() {
        assertEquals("", BundleParser.getLanr(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    // getEmail
    @Test
    void testGetPractitionerEmail() {
        assertEquals("beatriz.correia@incentergy.de", BundleParser.getEmail(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerEmailThatIsEmpty() {
        assertEquals("", BundleParser.getEmail(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyEmail() {
        assertEquals("beatriz.correia@incentergy.de", BundleParser.getEmail(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyEmailThatIsEmpty() {
        assertEquals("", BundleParser.getEmail(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getPhone
    @Test
    void testGetPractitionerPhone() {
        assertEquals("111222333", BundleParser.getPhone(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerPhoneThatIsEmpty() {
        assertEquals("", BundleParser.getPhone(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }
    @Test
    void testGetPharmacyPhone() {
        assertEquals("+49 123456789", BundleParser.getPhone(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyPhoneThatIsEmpty() {
        assertEquals("", BundleParser.getPhone(PHARMACY, this.fhirBundleWithEmptyFields));
    }

    // getFax
    @Test
    void testGetPractitionerFax() {
        assertEquals("11111111", BundleParser.getFax(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerFaxThatIsEmpty() {
        assertEquals("", BundleParser.getFax(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    // getModality
    @Test
    void testGetPractitionerModality() {
        assertEquals("isynet", BundleParser.getModality(PRACTITIONER, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPractitionerModalityThatIsEmpty() {
        assertEquals("", BundleParser.getModality(PRACTITIONER, this.fhirBundleWithEmptyFields));
    }

    // getBirthDate
    @Test
    void testGetPatientBirthDate() {
        assertEquals("1960-12-31", BundleParser.getBirthDate(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientBirthDateThatIsEmpty() {
        assertEquals("", BundleParser.getBirthDate(PATIENT, this.fhirBundleWithEmptyFields));
    }

    // getGender
    @Test
    void testGetPatientGender() {
        assertEquals("female", BundleParser.getGender(PATIENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPatientGenderIsEmpty() {
        assertEquals("", BundleParser.getGender(PATIENT, this.fhirBundleWithEmptyFields));
    }

    // getMedicationName
    @Test
    void testGetMedicationName() {
        assertEquals("BETAISODONA Salbe", BundleParser.getMedicationName(MEDICATIONSTATEMENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetMedicationNameThatIsEmpty() {
        assertEquals("", BundleParser.getMedicationName(MEDICATIONSTATEMENT, this.fhirBundleWithEmptyFields));
    }

    // getPzn
    @Test
    void testGetPzn() {
        assertEquals("8826490", BundleParser.getPzn(MEDICATIONSTATEMENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPznThatIsEmpty() {
        assertEquals("", BundleParser.getPzn(MEDICATIONSTATEMENT, this.fhirBundleWithEmptyFields));
    }

    // getDosage
    @Test
    void testGetDosage() {
        assertEquals("0-0-1-0", BundleParser.getDosage(MEDICATIONSTATEMENT, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetDosageThatIsEmpty() {
        assertEquals("", BundleParser.getDosage(MEDICATIONSTATEMENT, this.fhirBundleWithEmptyFields));
    }

    // getName
    @Test
    void testGetPharmacyName() {
        assertEquals("Apotheke 2", BundleParser.getName(PHARMACY, this.fhirBundleWithAllFields));
    }
    @Test
    void testGetPharmacyNameThatIsEmpty() {
        assertEquals("", BundleParser.getName(PHARMACY, this.fhirBundleWithEmptyFields));
    }
}
