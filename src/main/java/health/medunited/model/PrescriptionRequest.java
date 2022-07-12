package health.medunited.model;

public class PrescriptionRequest {
    private String practiceManagementTranslation;
    private String receiverPublicKeyFingerprint;
    private String fhirBundle;

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

    public void setPracticeManagementTranslation() {
        this.practiceManagementTranslation = practiceManagementTranslation;
    }

    public void setReceiverPublicKeyFingerprint() {
        this.receiverPublicKeyFingerprint = receiverPublicKeyFingerprint;
    }

    public void setFhirBundle() {
        this.fhirBundle = fhirBundle;
    }

}
