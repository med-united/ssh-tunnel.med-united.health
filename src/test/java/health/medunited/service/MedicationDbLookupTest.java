package health.medunited.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MedicationDbLookupTest {

    @Test
    void testSuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199");
        assertNotNull(tableEntry);
    }
    @Test
    void testUnsuccessfulLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("123456789");
        assertNull(tableEntry);
    }
    @Test
    void testGetMedicationName() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("Nifedipin Denk 20mg Retard", MedicationDbLookup.getMedicationName(tableEntry));
    }
    @Test
    void testGetQuantity() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("30 ST", MedicationDbLookup.getQuantity(tableEntry));
    }
    @Test
    void testGetPackageSize() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("N1", MedicationDbLookup.getPackageSize(tableEntry));
    }
    @Test
    void testGetAVP() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("16.99", MedicationDbLookup.getAVP(tableEntry));
    }
    @Test
    void testGetATC() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("C08CA05", MedicationDbLookup.getATC(tableEntry));
    }
    @Test
    void testGetComposition() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199"); // TODO: remove from here
        assert tableEntry != null;
        assertEquals("3835\\tNifedipin", MedicationDbLookup.getComposition(tableEntry));
    }
}
