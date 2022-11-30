package health.medunited.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MedicationDbLookupTest {

    List<String> tableEntryWithAllFields = Arrays.asList("384016", "2135106", "2X15 ST", "N1", "16.52", "Kalinor", "A12BA30", "11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat", "BTA", "Brausetabletten", "Desma GmbH");
    List<String> tableEntryWithSomeEmptyFields = Arrays.asList("921336", "10792640", "25X2 ST", "", "", "Medicomp Drain7.5x7.5cm St-Aca ", "", "", "KOM", "Kompressen", "Aca Müller/Adag Pharma AG");

    @Test
    void testSuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("2135106");
        assertNotNull(tableEntry);
        assertEquals(11, tableEntry.size());
        assertEquals(tableEntry, tableEntryWithAllFields);
    }
    @Test
    void testUnsuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("123456789");
        assertNull(tableEntry);
    }
    @Test
    void testGetMedicationName() {
        assertEquals("Kalinor", MedicationDbLookup.getMedicationName(tableEntryWithAllFields));
    }
    @Test
    void testGetMedicationNameWithSpaceAtTheEnd() {
        assertEquals("Medicomp Drain7.5x7.5cm St-Aca", MedicationDbLookup.getMedicationName(tableEntryWithSomeEmptyFields));
    }
    @Test
    void testGetQuantity() {
        assertEquals("2X15 ST", MedicationDbLookup.getQuantity(tableEntryWithAllFields));
    }
    @Test
    void testGetPackageSize() {
        assertEquals("N1", MedicationDbLookup.getPackageSize(tableEntryWithAllFields));
    }
    @Test
    void testGetEmptyPackageSize() {
        assertEquals("", MedicationDbLookup.getPackageSize(tableEntryWithSomeEmptyFields));
    }
    @Test
    void testGetAVP() {
        assertEquals("16.52", MedicationDbLookup.getAVP(tableEntryWithAllFields));
    }
    @Test
    void testGetEmptyAVP() {
        assertEquals("", MedicationDbLookup.getAVP(tableEntryWithSomeEmptyFields));
    }
    @Test
    void testGetATC() {
        assertEquals("A12BA30", MedicationDbLookup.getATC(tableEntryWithAllFields));
    }
    @Test
    void testGetEmptyATC() {
        assertEquals("", MedicationDbLookup.getATC(tableEntryWithSomeEmptyFields));
    }
    @Test
    void testGetComposition() {
        assertEquals("11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat", MedicationDbLookup.getComposition(tableEntryWithAllFields));
    }
    @Test
    void testGetEmptyComposition() {
        assertEquals("", MedicationDbLookup.getComposition(tableEntryWithSomeEmptyFields));
    }
    @Test
    void testGetPharmaceuticalFormCode() {
        assertEquals("BTA", MedicationDbLookup.getPharmaceuticalFormCode(tableEntryWithAllFields));
    }
    @Test
    void testGetPharmaceuticalFormText() {
        assertEquals("Brausetabletten", MedicationDbLookup.getPharmaceuticalFormText(tableEntryWithAllFields));
    }
    @Test
    void testGetManufacturer() {
        assertEquals("Desma GmbH", MedicationDbLookup.getManufacturer(tableEntryWithAllFields));
    }
}
