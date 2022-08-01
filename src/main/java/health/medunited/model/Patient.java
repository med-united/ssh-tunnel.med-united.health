package health.medunited.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private final String firstName;
    private final String lastName;
    private final String street;
    private final String houseNumber;
    private final String city;
    private final String postalCode;
    private final String gender;
    private final String birthDate;

    public Patient(String firstName, String lastName, String street, String houseNumber, String city, String postalCode, String gender, String birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.gender = gender;
        this.birthDate = birthDate;
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
    public String getGender() {
        return gender;
    }
    public String getBirthDate() {
        return birthDate;
    }
}
