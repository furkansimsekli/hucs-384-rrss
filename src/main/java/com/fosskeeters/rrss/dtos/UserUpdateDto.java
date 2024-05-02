package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.User;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

public class UserUpdateDto {
    @NotNull
    @Size(min = 1, max = 64, message = "First name must be between 1-64 characters!")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 64, message = "Last name must be between 1-64 characters!")
    private String lastName;

    @NotNull
    @Size(min = 5, max = 160, message = "Email address must be between 5-160 characters!")
    @Email
    private String email;

    @NotNull
    @Size(min = 1, max = 64, message = "Phone number must be between 1-15 characters!")
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Size(min = 0, max = 256, message = "Address must be 256 characters at most!")
    private String address;

    @NotNull
    private String profileImagePath;

    // @NotNull()
    // @Size(min = 1, max = 256, message = "Password length must be between 1-256
    // characters!")
    // private String password1;

    // @NotNull
    // @Size(min = 1, max = 256, message = "Password length must be between 1-256
    // characters!")
    // private String password2;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = toTitleCase(firstName.trim());
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = toTitleCase(lastName.trim());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber.trim();
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

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public static UserUpdateDto fromUser(User user) {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImagePath(user.getProfileImagePath());
        return dto;
    }

    // public String getPassword1() {
    // return password1;
    // }

    // public void setPassword1(String password1) {
    // this.password1 = password1;
    // }

    // public String getPassword2() {
    // return password2;
    // }

    // public void setPassword2(String password2) {
    // this.password2 = password2;
    // }

    // TODO: move to a global utils library
    /**
     * Converts the given string to title case. In title case, the first letter of
     * each word is
     * converted to uppercase, while the rest of the letters are converted to
     * lowercase. Whitespace
     * (e.g. spaces, tabs) separates words.
     *
     * @param text The input string to be converted.
     * @return A new string in title case, or the original string if it's null or
     *         empty.
     */
    private static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();
        boolean convertNext = true;

        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }
}
