package health.medunited.model;

public class MedicationStatement {
    private final String medicationName;
    private final String PZN;
    private final String dosage;

    public MedicationStatement(String medicationName, String PZN, String dosage) {
        this.medicationName = medicationName;
        this.PZN = PZN;
        this.dosage = dosage;
    }

    public String getMedicationName() {
        return medicationName;
    }
    public String getPZN() {
        return PZN;
    }
    public String getDosage() {
        return dosage;
    }
}
