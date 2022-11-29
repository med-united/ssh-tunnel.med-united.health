package health.medunited.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MedicationDbLookupTest {

    List<String> tableEntryWithAllFields_db1 = Arrays.asList("384016", "2135106", "2X15 ST", "N1", "16.52", "Kalinor", "A12BA30", "BTA", "Brausetabletten", "Desma GmbH");
    List<String> tableEntryWithAllFields_db2 = Arrays.asList("2135106", "11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat");

    List<String> tableEntryWithSomeEmptyFields_db1 = Arrays.asList("921336", "10792640", "25X2 ST", "", "", "Medicomp Drain7.5x7.5cm St-Aca ", "", "KOM", "Kompressen", "Aca Müller/Adag Pharma AG");
    List<String> tableEntryWithSomeEmptyFields_db2 = Arrays.asList("10792640", "");

    @Test
    void testSuccessfulLookupMedicationByPZN() {
        List<String> tableEntry1 = MedicationDbLookup.lookupMedicationByPZN_db1("2135106");
        List<String> tableEntry2 = MedicationDbLookup.lookupMedicationByPZN_db2("2135106");
        assertNotNull(tableEntry1);
        assertEquals(10, tableEntry1.size());
        assertEquals(tableEntry1, tableEntryWithAllFields_db1);
        assertNotNull(tableEntry2);
        assertEquals(2, tableEntry2.size());
        assertEquals(tableEntry2, tableEntryWithAllFields_db2);
    }
    @Test
    void testUnsuccessfulLookupMedicationByPZN() {
        List<String> tableEntryWithSomeEmptyFields_db1 = MedicationDbLookup.lookupMedicationByPZN_db1("123456789");
        assertNull(tableEntryWithSomeEmptyFields_db1);
    }
    @Test
    void testGetMedicationName() {
        assertEquals("Kalinor", MedicationDbLookup.getMedicationName(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetMedicationNameWithSpaceAtTheEnd() {
        assertEquals("Medicomp Drain7.5x7.5cm St-Aca", MedicationDbLookup.getMedicationName(tableEntryWithSomeEmptyFields_db1));
    }
    @Test
    void testGetQuantity() {
        assertEquals("2X15 ST", MedicationDbLookup.getQuantity(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetPackageSize() {
        assertEquals("N1", MedicationDbLookup.getPackageSize(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetEmptyPackageSize() {
        assertEquals("", MedicationDbLookup.getPackageSize(tableEntryWithSomeEmptyFields_db1));
    }
    @Test
    void testGetAVP() {
        assertEquals("16.52", MedicationDbLookup.getAVP(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetEmptyAVP() {
        assertEquals("", MedicationDbLookup.getAVP(tableEntryWithSomeEmptyFields_db1));
    }
    @Test
    void testGetATC() {
        assertEquals("A12BA30", MedicationDbLookup.getATC(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetEmptyATC() {
        assertEquals("", MedicationDbLookup.getATC(tableEntryWithSomeEmptyFields_db1));
    }
    @Test
    void testGetComposition() {
        assertEquals("11288\\tCitronensäure; wasserfrei\\n10441\\tKalium citrat 1-Wasser\\n10455\\tKalium hydrogencarbonat", MedicationDbLookup.getComposition(tableEntryWithAllFields_db2));
    }
    @Test
    void testGetEmptyComposition() {
        assertEquals("", MedicationDbLookup.getComposition(tableEntryWithSomeEmptyFields_db2));
    }
    @Test
    void testGetPharmaceuticalFormCode() {
        assertEquals("BTA", MedicationDbLookup.getPharmaceuticalFormCode(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetPharmaceuticalFormText() {
        assertEquals("Brausetabletten", MedicationDbLookup.getPharmaceuticalFormText(tableEntryWithAllFields_db1));
    }
    @Test
    void testGetManufacturer() {
        assertEquals("Desma GmbH", MedicationDbLookup.getManufacturer(tableEntryWithAllFields_db1));
    }
}
