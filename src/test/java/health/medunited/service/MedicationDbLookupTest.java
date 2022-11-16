package health.medunited.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MedicationDbLookupTest {

    List<String> tableEntryForTesting = Arrays.asList("384016", "2135106", "2X15 ST", "N1", "16.52", "Kalinor", "A12BA30", "11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat", "BTA", "Brausetabletten", "Desma GmbH");

    @Test
    void testSuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("2135106");
        assertNotNull(tableEntry);
        assertTrue(tableEntry.size() == 11);
        assertEquals(tableEntry, tableEntryForTesting);
    }
    @Test
    void testUnsuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("123456789");
        assertNull(tableEntry);
    }
    @Test
    void testGetMedicationName() {
        assertEquals("Kalinor", MedicationDbLookup.getMedicationName(tableEntryForTesting));
    }
    @Test
    void testGetQuantity() {
        assertEquals("2X15 ST", MedicationDbLookup.getQuantity(tableEntryForTesting));
    }
    @Test
    void testGetPackageSize() {
        assertEquals("N1", MedicationDbLookup.getPackageSize(tableEntryForTesting));
    }
    @Test
    void testGetAVP() {
        assertEquals("16.52", MedicationDbLookup.getAVP(tableEntryForTesting));
    }
    @Test
    void testGetATC() {
        assertEquals("A12BA30", MedicationDbLookup.getATC(tableEntryForTesting));
    }
    @Test
    void testGetComposition() {
        assertEquals("11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat", MedicationDbLookup.getComposition(tableEntryForTesting));
    }
    @Test
    void testGetPharmaceuticalFormCode() {
        assertEquals("BTA", MedicationDbLookup.getPharmaceuticalFormCode(tableEntryForTesting));
    }
    @Test
    void testGetPharmaceuticalFormText() {
        assertEquals("Brausetabletten", MedicationDbLookup.getPharmaceuticalFormText(tableEntryForTesting));
    }
    @Test
    void testGetManufacturer() {
        assertEquals("Desma GmbH", MedicationDbLookup.getManufacturer(tableEntryForTesting));
    }
}
