package health.medunited.service;

import org.hl7.fhir.r4.model.*;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import java.util.Calendar;
import java.util.Objects;

public class BundleParser {

    private static final String PRACTITIONER = "practitioner";
    private static final String PATIENT = "patient";
    private static final String MEDICATIONSTATEMENT = "medicationStatement";
    private static final String PHARMACY = "organization";

    public static Bundle parseBundle(String fhirBundle) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.parseResource(org.hl7.fhir.r4.model.Bundle.class, fhirBundle);
    }

    public static String getFirstName(String resourceType, Bundle parsedBundle) {
        String firstName = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasName() && practitioner.getName().get(0).hasGiven()) {
            firstName = practitioner.getName().get(0).getGiven().get(0).toString();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasName() && patient.getName().get(0).hasGiven()) {
            firstName = patient.getName().get(0).getGiven().get(0).toString();
        }
        return firstName;
    }

    public static String getLastName(String resourceType, Bundle parsedBundle) {
        String familyName = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasName() && practitioner.getName().get(0).hasFamily()) {
            familyName = practitioner.getName().get(0).getFamily();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasName() && patient.getName().get(0).hasFamily()) {
            familyName = patient.getName().get(0).getFamily();
        }
        return familyName;
    }

    public static String getStreet(String resourceType, Bundle parsedBundle) {
        String addressLine = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasAddress() && practitioner.getAddress().get(0).hasLine()) {
            addressLine = practitioner.getAddress().get(0).getLine().get(0).toString();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasAddress() && patient.getAddress().get(0).hasLine()) {
            addressLine = patient.getAddress().get(0).getLine().get(0).toString();
        }
        else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasAddress() && pharmacy.getAddress().get(0).hasLine()) {
            addressLine = pharmacy.getAddress().get(0).getLine().get(0).toString();
        }
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder streetLine = new StringBuilder();
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            for (int i = 1; i < addressParts.length; i++) {
                streetLine.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            for (int i = 0; i < addressParts.length - 1; i++) {
                streetLine.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            streetLine = new StringBuilder(addressLine);
        }
        return streetLine.toString().trim();
    }

    public static String getHouseNumber(String resourceType, Bundle parsedBundle) {
        String addressLine = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasAddress() && practitioner.getAddress().get(0).hasLine()) {
            addressLine = practitioner.getAddress().get(0).getLine().get(0).toString();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasAddress() && patient.getAddress().get(0).hasLine()) {
            addressLine = patient.getAddress().get(0).getLine().get(0).toString();
        }
        else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasAddress() && pharmacy.getAddress().get(0).hasLine()) {
            addressLine = pharmacy.getAddress().get(0).getLine().get(0).toString();
        }
        String[] addressParts = addressLine.trim().split("\\s+");
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
        } else {
            // no houseNumber in the addressLine
        }
        return houseNumber;
    }

    public static String getCity(String resourceType, Bundle parsedBundle) {
        String city = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasAddress() && practitioner.getAddress().get(0).hasCity()) {
            city = practitioner.getAddress().get(0).getCity();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasAddress() && patient.getAddress().get(0).hasCity()) {
            city = patient.getAddress().get(0).getCity();
        }
        else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasAddress() && pharmacy.getAddress().get(0).hasCity()) {
            city = pharmacy.getAddress().get(0).getCity();
        }
        return city;
    }

    public static String getPostalCode(String resourceType, Bundle parsedBundle) {
        String postalCode = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasAddress() && practitioner.getAddress().get(0).hasPostalCode()) {
            postalCode = practitioner.getAddress().get(0).getPostalCode();
        }
        else if (Objects.equals(resourceType, PATIENT) && patient.hasAddress() && patient.getAddress().get(0).hasPostalCode()) {
            postalCode = patient.getAddress().get(0).getPostalCode();
        }
        else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasAddress() && pharmacy.getAddress().get(0).hasPostalCode()) {
            postalCode = pharmacy.getAddress().get(0).getPostalCode();
        }
        return postalCode;
    }

    public static String getLanr(String resourceType, Bundle parsedBundle) {
        String lanr = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasIdentifier() && practitioner.getIdentifier().get(0).hasValue()) {
            lanr = practitioner.getIdentifier().get(0).getValue();
        }
        return lanr;
    }

    public static String getEmail(String resourceType, Bundle parsedBundle) {
        String email = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        // TODO: currently there is no distinction between the e-mail, phone and fax, ["system": "email"] and the others should be added in the frontend definition
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasTelecom() && practitioner.getTelecom().size() == 3 && practitioner.getTelecom().get(0).hasValue()) {
            email = practitioner.getTelecom().get(0).getValue();
        } else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasTelecom() && pharmacy.getTelecom().size() == 2 && pharmacy.getTelecom().get(1).hasValue()) {
            email = pharmacy.getTelecom().get(1).getValue();
        } else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasTelecom() && pharmacy.getTelecom().size() == 1 && pharmacy.getTelecom().get(0).hasValue() && pharmacy.getTelecom().get(0).getValue().contains("@")) {
            email = pharmacy.getTelecom().get(1).getValue();
        }
        return email;
    }

    public static String getPhone(String resourceType, Bundle parsedBundle) {
        String phone = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        // TODO: currently there is no distinction between the e-mail, phone and fax, ["system": "phone"] and the others should be added in the frontend definition
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasTelecom() && practitioner.getTelecom().size() == 3 && practitioner.getTelecom().get(1).hasValue()) {
            phone = practitioner.getTelecom().get(1).getValue();
        } else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasTelecom() && pharmacy.getTelecom().size() == 2 && pharmacy.getTelecom().get(0).hasValue()) {
            phone = pharmacy.getTelecom().get(0).getValue();
        } else if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasTelecom() && pharmacy.getTelecom().size() == 1 && pharmacy.getTelecom().get(0).hasValue() && !pharmacy.getTelecom().get(0).getValue().contains("@")) {
            phone = pharmacy.getTelecom().get(0).getValue();
        }
        return phone;
    }

    public static String getFax(String resourceType, Bundle parsedBundle) {
        String fax = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        // TODO: currently there is no distinction between the e-mail, phone and fax, ["system": "fax"] and the others should be added in the frontend definition
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasTelecom() && practitioner.getTelecom().size() == 3 && practitioner.getTelecom().get(2).hasValue()) {
            fax = practitioner.getTelecom().get(2).getValue();
        }
        return fax;
    }

    public static String getModality(String resourceType, Bundle parsedBundle) {
        String modality = "";
        Practitioner practitioner = (Practitioner) parsedBundle.getEntry().get(0).getResource();
        if (Objects.equals(resourceType, PRACTITIONER) && practitioner.hasExtension() && practitioner.getExtension().get(0).hasValue()) {
            modality = practitioner.getExtension().get(0).getValue().toString();
        }
        return modality;
    }

    public static String getBirthDate(String resourceType, Bundle parsedBundle) {
        String birthDate = "";
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        if (Objects.equals(resourceType, PATIENT) && patient.hasBirthDate()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(patient.getBirthDate());
            int monthIndexAdjusted = cal.get(Calendar.MONTH) + 1;
            String sMonth = "" + monthIndexAdjusted;
            if (monthIndexAdjusted < 10) {
                sMonth = "0" + sMonth;
            } else {
                sMonth = "" + sMonth;
            }
            String sDay;
            if (cal.get(Calendar.DAY_OF_MONTH) < 10) {
                sDay = "0" + cal.get(Calendar.DAY_OF_MONTH);
            } else {
                sDay = "" + cal.get(Calendar.DAY_OF_MONTH);
            }
            birthDate = cal.get(Calendar.YEAR) + "-" + sMonth + "-" + sDay;
        }
        return birthDate;
    }

    public static String getGender(String resourceType, Bundle parsedBundle) {
        String gender = "";
        Patient patient = (Patient) parsedBundle.getEntry().get(1).getResource();
        if (Objects.equals(resourceType, PATIENT) && patient.hasGender()) {
            gender = patient.getGender().toString().toLowerCase();
        }
        return gender;
    }

    public static String getMedicationName(String resourceType, Bundle parsedBundle) {
        String medicationName = "";
        MedicationStatement medicationStatement = (MedicationStatement) parsedBundle.getEntry().get(2).getResource();
        if (Objects.equals(resourceType, MEDICATIONSTATEMENT) && medicationStatement.hasMedicationCodeableConcept() && medicationStatement.getMedicationCodeableConcept().hasText()) {
            medicationName = medicationStatement.getMedicationCodeableConcept().getText();
        }
        return medicationName;
    }

    public static String getPzn(String resourceType, Bundle parsedBundle) {
        String pzn = "";
        MedicationStatement medicationStatement = (MedicationStatement) parsedBundle.getEntry().get(2).getResource();
        if (Objects.equals(resourceType, MEDICATIONSTATEMENT) && medicationStatement.hasIdentifier() && medicationStatement.getIdentifier().get(0).hasValue()) {
            pzn = medicationStatement.getIdentifier().get(0).getValue();
        }
        return pzn;
    }

    public static String getDosage(String resourceType, Bundle parsedBundle) {
        String dosage = "";
        MedicationStatement medicationStatement = (MedicationStatement) parsedBundle.getEntry().get(2).getResource();
        if (Objects.equals(resourceType, MEDICATIONSTATEMENT) && medicationStatement.hasDosage()) {
            dosage = medicationStatement.getDosage().get(0).getText();
        }
        return dosage;
    }

    public static String getName(String resourceType, Bundle parsedBundle) {
        String name = "";
        Organization pharmacy = (Organization) parsedBundle.getEntry().get(3).getResource();
        if (Objects.equals(resourceType, PHARMACY) && pharmacy.hasName()) {
            name = pharmacy.getName();
        }
        return name;
    }
}
