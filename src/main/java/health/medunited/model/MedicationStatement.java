package health.medunited.model;

public class MedicationStatement {
    private final String medicationName;
    private final int PZN;
    private final String dosage;
    private final String note;

    public MedicationStatement(String medicationName, int PZN, String dosage, String note) {
        this.medicationName = medicationName;
        this.PZN = PZN;
        this.dosage = dosage;
        this.note = note;
    }

    public String getMedicationName() {
        return medicationName;
    }
    public int getPZN() {
        return PZN;
    }
    public String getDosage() {
        return dosage;
    }
    public String getNote() {
        return note;
    }
}
