package health.medunited.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private final String firstName;
    private final String lastName;
    private final String street;
    private final int houseNumber;
    private final String city;
    private final int postalCode;
    private final String gender;
    private final String birthDate;
    private final String email;
    private final int phone;
    private final Practitioner generalPractitioner;

    public Patient(String firstName, String lastName, String street, int houseNumber, String city, int postalCode, String gender, String birthDate, String email, int phone, Practitioner generalPractitioner) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.generalPractitioner = generalPractitioner;
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
    public String getGender() {
        return gender;
    }
    public String getBirthDate() {
        return birthDate;
    }
    public String getEmail() {
        return email;
    }
    public int getPhone() {
        return phone;
    }
    public Practitioner getGeneralPractitioner() {
        return generalPractitioner;
    }

    public void setFirstName() {
        this.firstName = firstName;
    }
    public void setLastName() {
        this.lastName = lastName;
    }
    public void setStreet() {
        this.street = street;
    }
    public void setHouseNumber() {
        this.houseNumber = houseNumber;
    }
    public void setCity() {
        this.city = city;
    }
    public void setPostalCode() {
        this.postalCode = postalCode;
    }
    public void setGender() {
        this.gender = gender;
    }
    public void setBirthDate() {
        this.birthDate = birthDate;
    }
    public void setEmail() {
        this.email = email;
    }
    public void setPhone() {
        this.phone = phone;
    }
    public void setGeneralPractitioner() {
        this.generalPractitioner = generalPractitioner;
    }
}
