package health.medunited.model;

public class PrescriptionRequest {
    private final String practiceManagementTranslation;
    private final String receiverPublicKeyFingerprint;
    private final String fhirBundle;

    public PrescriptionRequest(String practiceManagementTranslation, String receiverPublicKeyFingerprint, String fhirBundle) {
        this.practiceManagementTranslation = practiceManagementTranslation;
        this.receiverPublicKeyFingerprint = receiverPublicKeyFingerprint;
        this.fhirBundle = fhirBundle;
    }

    public String getPracticeManagementTranslation() {
        return practiceManagementTranslation;
    }

    public String getReceiverPublicKeyFingerprint() {
        return receiverPublicKeyFingerprint;
    }

    public String getFhirBundle() {
        return fhirBundle;
    }

}
