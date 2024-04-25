package com.fosskeeters.rrss.models;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(unique = true, length = 16)
    private String username;

    @Column private String password;

    @Column private int type; // 1: Administrator, 2: Merchant, 3: Customer, 4: Moderator

    @Column(unique = true, length = 160)
    private String email;

    @Column(unique = true, length = 15)
    private String phoneNumber;

    @Column private LocalDate dateOfBirth;

    @Column(length = 256)
    private String address;

    @Column private String profileImagePath;

    @CreatedDate private LocalDateTime createdAt;

    public User(
            String firstName,
            String lastName,
            String username,
            String password,
            int type,
            String email,
            String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.type = type;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.address = "";
        this.profileImagePath = "path/to/default/image";
        this.dateOfBirth = LocalDate.of(1970, 1, 1);
    }

    public User() {
        this.createdAt = LocalDateTime.now();
        this.address = "";
        this.profileImagePath = "path/to/default/image";
        this.dateOfBirth = LocalDate.of(1970, 1, 1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
