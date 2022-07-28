package health.medunited.model;

public class Practitioner {
    private final String firstName;
    private final String lastName;
    private final String street;
    private final int houseNumber;
    private final String city;
    private final int postalCode;
    private final String email;
    private final int phone;
    private final String modality;

    public Practitioner(String firstName, String lastName, String street, int houseNumber, String city, int postalCode, String email, int phone, String modality) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.email = email;
        this.phone = phone;
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
    public int getHouseNumber() {
        return houseNumber;
    }
    public String getCity() {
        return city;
    }
    public int getPostalCode() {
        return postalCode;
    }
    public String getEmail() {
        return email;
    }
    public int getPhone() {
        return phone;
    }
    public String getModality() {
        return modality;
    }
}
