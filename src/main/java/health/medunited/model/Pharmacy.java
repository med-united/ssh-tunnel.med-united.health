package health.medunited.model;

public class Pharmacy {
    private final String name;
    private final String street;
    private final String houseNumber;
    private final String city;
    private final String postalCode;
    private final String phone;
    private final String email;

    public Pharmacy(String name, String street, String houseNumber, String city, String postalCode, String phone, String email) {
        this.name = name;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
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
    public String getPhone() {
        return phone;
    }
    public String getEmail() {
        return email;
    }
}
