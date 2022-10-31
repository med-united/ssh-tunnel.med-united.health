package health.medunited.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

public class MedicationDbLookupTest {

    @Test
    public void testLookupMedicationByPZN() {
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN("17952199");
        assertNotNull(tableEntry);
        assertEquals("Nifedipin Denk 20mg Retard", MedicationDbLookup.getMedicationName(tableEntry));
    }
}
