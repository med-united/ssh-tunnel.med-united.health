package health.medunited.model;

public class Bundle {
    private final Practitioner practitioner;
    private final Patient patient;
    private final MedicationStatement medicationStatement;
    private final Pharmacy pharmacy;

    public Bundle(Practitioner practitioner, Patient patient, MedicationStatement medicationStatement, Pharmacy pharmacy) {
        this.practitioner = practitioner;
        this.patient = patient;
        this.medicationStatement = medicationStatement;
        this.pharmacy = pharmacy;
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
    public Pharmacy getPharmacy() {
        return pharmacy;
    }
}