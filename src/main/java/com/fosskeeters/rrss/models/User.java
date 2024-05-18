package com.fosskeeters.rrss.models;

import com.fosskeeters.rrss.dtos.UserDto;
import com.fosskeeters.rrss.dtos.UserUpdateDto;

import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class User {
    public enum Type { CUSTOMER, MERCHANT, ADMIN, COMMUNITY_MOD }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(unique = true, length = 16)
    private String username;

    @Column
    private String password;

    @Column
    private Type type;

    @Column(unique = true, length = 160)
    private String email;

    @Column(unique = true, length = 15)
    private String phoneNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column(length = 256)
    private String address;

    @Column
    private String profileImagePath;

    @Column
    private boolean isApproved;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", cascade = CascadeType.ALL)
    private List<BrowsingHistory> browsingHistory;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL)
    private List<Entry> entries;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Topic> topics;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private List<Vote> votes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Wish> wishes;

    public User(String firstName, String lastName, String username, String password, Type type,
                String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.type = type;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.address = "";
        this.profileImagePath = "/filler-user.png";
        this.dateOfBirth = LocalDate.of(1970, 1, 1);
        this.isApproved = false;
    }

    public User() {
        this.createdAt = LocalDateTime.now();
        this.address = "";
        this.profileImagePath = "/filler-user.png";
        this.dateOfBirth = LocalDate.of(1970, 1, 1);
        this.isApproved = false;
    }

    public User(UserDto userDto, String encodedPassword) {
        this.firstName = userDto.getFirstName();
        this.lastName = userDto.getLastName();
        this.username = userDto.getUsername();
        this.type = userDto.getAccountType().equals("customer") ? User.Type.CUSTOMER
                                                                : User.Type.MERCHANT;
        this.email = userDto.getEmail();
        this.phoneNumber = userDto.getPhoneNumber();
        this.createdAt = LocalDateTime.now();
        this.address = "";
        this.profileImagePath = "/filler-user.png";
        this.dateOfBirth = LocalDate.of(1970, 1, 1);
        this.password = encodedPassword;
        this.isApproved = false;
    }

    public void setFromUserUpdateDto(UserUpdateDto userUpdateDto) {
        this.firstName = userUpdateDto.getFirstName();
        this.lastName = userUpdateDto.getLastName();
        this.email = userUpdateDto.getEmail();
        this.phoneNumber = userUpdateDto.getPhoneNumber();
        this.address = userUpdateDto.getAddress();
        this.dateOfBirth = userUpdateDto.getDateOfBirth();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<BrowsingHistory> getBrowsingHistory() {
        return browsingHistory;
    }

    public void setBrowsingHistory(List<BrowsingHistory> browsingHistory) {
        this.browsingHistory = browsingHistory;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public List<Wish> getWishes() {
        return wishes;
    }

    public void setWishes(List<Wish> wishes) {
        this.wishes = wishes;
    }
}
