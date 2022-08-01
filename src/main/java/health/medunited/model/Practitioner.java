package health.medunited.model;

public class Practitioner {
    private final String firstName;
    private final String lastName;
    private final String street;
    private final String houseNumber;
    private final String city;
    private final String postalCode;
    private final String email;
    private final String phone;
    private final String fax;
    private final String modality;

    public Practitioner(String firstName, String lastName, String street, String houseNumber, String city, String postalCode, String email, String  phone, String fax, String modality) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.email = email;
        this.phone = phone;
        this.fax = fax;
        this.modality = modality;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getStreet() {
        return street;
    }
    public String getHouseNumber() {
        return houseNumber;
    }
    public String getCity() {
        return city;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getFax() {
        return phone;
    }
    public String getModality() {
        return modality;
    }
}
