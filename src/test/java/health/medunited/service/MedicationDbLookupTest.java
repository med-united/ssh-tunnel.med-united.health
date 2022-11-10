package health.medunited.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MedicationDbLookupTest {

    List<String> tableEntryForTesting = Arrays.asList("1390624", "17952199", "30 ST", "N1", "16.99", "Nifedipin Denk 20mg Retard", "C08CA05", "3835\\tNifedipin");

    @Test
    void testSuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199");
        assertNotNull(tableEntry);
        assertEquals(tableEntry, tableEntryForTesting);
    }
    @Test
    void testUnsuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("123456789");
        assertNull(tableEntry);
    }
    @Test
    void testGetMedicationName() {
        assertEquals("Nifedipin Denk 20mg Retard", MedicationDbLookup.getMedicationName(tableEntryForTesting));
    }
    @Test
    void testGetQuantity() {
        assertEquals("30 ST", MedicationDbLookup.getQuantity(tableEntryForTesting));
    }
    @Test
    void testGetPackageSize() {
        assertEquals("N1", MedicationDbLookup.getPackageSize(tableEntryForTesting));
    }
    @Test
    void testGetAVP() {
        assertEquals("16.99", MedicationDbLookup.getAVP(tableEntryForTesting));
    }
    @Test
    void testGetATC() {
        assertEquals("C08CA05", MedicationDbLookup.getATC(tableEntryForTesting));
    }
    @Test
    void testGetComposition() {
        assertEquals("3835\\tNifedipin", MedicationDbLookup.getComposition(tableEntryForTesting));
    }
}
