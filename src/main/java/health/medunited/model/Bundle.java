package health.medunited.model;

public class Bundle {
    private final Practitioner practitioner;
    private final Patient patient;
    private final MedicationStatement medicationStatement;

    public Bundle(Practitioner practitioner, Patient patient, MedicationStatement medicationStatement) {
        this.practitioner = practitioner;
        this.patient = patient;
        this.medicationStatement = medicationStatement;
    }

    public Practitioner getPractitioner() {
        return practitioner;
    }
    public Patient getPatient() {
        return patient;
    }
    public MedicationStatement getMedicationStatement() {
        return medicationStatement;
    }
}
