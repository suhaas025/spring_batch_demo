package com.example.Spring_batch_demo.dto;

import com.opencsv.bean.CsvBindByName;

public class CustomerCSV {
    
    @CsvBindByName(column = "first_name")
    private String firstName;
    
    @CsvBindByName(column = "last_name")
    private String lastName;
    
    @CsvBindByName(column = "email")
    private String email;
    
    @CsvBindByName(column = "age")
    private String age;
    
    @CsvBindByName(column = "city")
    private String city;
    
    // Constructors
    public CustomerCSV() {}
    
    public CustomerCSV(String firstName, String lastName, String email, String age, String city) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.city = city;
    }
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAge() {
        return age;
    }
    
    public void setAge(String age) {
        this.age = age;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    @Override
    public String toString() {
        return "CustomerCSV{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", age='" + age + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
} 