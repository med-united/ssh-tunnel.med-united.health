package health.medunited.service;

import health.medunited.model.MedicationStatement;
import health.medunited.model.Patient;
import health.medunited.model.Pharmacy;
import health.medunited.model.Practitioner;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JsonExtractor {

    public Practitioner parsePractitioner(JSONObject practitionerJSON) {

        JSONObject practitionerResource = practitionerJSON.getJSONObject("resource");

        JSONObject name = (JSONObject) practitionerResource.getJSONArray("name").get(0);
        String firstName = ((JSONArray) name.get("given")).get(0).toString();
        String lastName = name.get("family").toString();

        JSONObject address = (JSONObject) practitionerResource.getJSONArray("address").get(0);
        String addressLine = ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }
        String city = address.get("city").toString();
        String postalCode = address.get("postalCode").toString();

        String email = "";
        String phone = "";
        String fax = "";
//        TODO: currently there is no distinction between the e-mail, phone and fax, ["system": "phone"] and the others should be added in the frontend definition
        if (practitionerResource.has("telecom")) {
            if (practitionerResource.getJSONArray("telecom").length() == 3) {
                email = ((JSONObject) practitionerResource.getJSONArray("telecom").get(0)).get("value").toString();
                phone = ((JSONObject) practitionerResource.getJSONArray("telecom").get(1)).get("value").toString();
                fax = ((JSONObject) practitionerResource.getJSONArray("telecom").get(2)).get("value").toString();
            }
        }

        String modality = ((JSONObject) practitionerResource.getJSONArray("extension").get(0)).get("valueString").toString();

        return new Practitioner(firstName, lastName, street.toString().trim(), houseNumber, city, postalCode, email, phone, fax, modality);
    }

    public Patient parsePatient(JSONObject patientJSON) {

        JSONObject patientResource = patientJSON.getJSONObject("resource");

        JSONObject name = (JSONObject) patientResource.getJSONArray("name").get(0);
        String firstName = ((JSONArray) name.get("given")).get(0).toString();
        String lastName = name.get("family").toString();

        JSONObject address = (JSONObject) patientResource.getJSONArray("address").get(0);
        String addressLine = ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }
        String city = address.get("city").toString();
        String postalCode = address.get("postalCode").toString();
        String gender = patientResource.get("gender").toString();
        String birthDate = patientResource.get("birthDate").toString();

        return new Patient(firstName, lastName, street.toString().trim(), houseNumber, city, postalCode, gender, birthDate);
    }

    public MedicationStatement parseMedicationStatement(JSONObject medicationStatementJSON) {

        JSONObject medicationStatementResource = medicationStatementJSON.getJSONObject("resource");

        String medicationName = medicationStatementResource.getJSONObject("medicationCodeableConcept").get("text").toString();
        String PZN = ((JSONObject) medicationStatementResource.getJSONArray("identifier").get(0)).get("value").toString();
        String dosage = ((JSONObject) medicationStatementResource.getJSONArray("dosage").get(0)).get("text").toString();

        return new MedicationStatement(medicationName, PZN, dosage);
    }

    public Pharmacy parsePharmacy(JSONObject pharmacyJSON) {

        JSONObject pharmacyResource = pharmacyJSON.getJSONObject("resource");

        String name = pharmacyResource.get("name").toString();

        JSONObject address = (JSONObject) pharmacyResource.getJSONArray("address").get(0);
        String addressLine = ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }

        String city = address.get("city").toString();
        String postalCode = address.get("postalCode").toString();

        String phone = "";
        String email = "";
        if (pharmacyResource.has("telecom")) {
            if (pharmacyResource.getJSONArray("telecom").length() == 2) {
                phone = ((JSONObject) pharmacyResource.getJSONArray("telecom").get(0)).get("value").toString();
                email = ((JSONObject) pharmacyResource.getJSONArray("telecom").get(1)).get("value").toString();
            }
            if (pharmacyResource.getJSONArray("telecom").length() == 1) {
                JSONObject phoneOrEmail = (JSONObject) pharmacyResource.getJSONArray("telecom").get(0);
                if (phoneOrEmail.get("value").toString().contains("@")) {
                    email = phoneOrEmail.get("value").toString();
                } else {
                    phone = phoneOrEmail.get("value").toString();
                }
            }
        }
        return new Pharmacy(name, street.toString().trim(), houseNumber, city, postalCode, phone, email);
    }
}
