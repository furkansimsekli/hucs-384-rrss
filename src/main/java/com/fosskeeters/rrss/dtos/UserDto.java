package com.fosskeeters.rrss.dtos;

import jakarta.validation.constraints.*;

public class UserDto {
    @NotNull
    @Size(min = 1, max = 64, message = "First name must be between 1-64 characters!")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 64, message = "Last name must be between 1-64 characters!")
    private String lastName;

    @NotNull
    @Size(min = 1, max = 64, message = "Username must be between 1-16 characters!")
    private String username;

    @NotNull
    @Size(min = 5, max = 160, message = "Email address must be between 5-160 characters!")
    @Email
    private String email;

    @NotNull
    @Size(min = 1, max = 64, message = "Phone number must be between 1-15 characters!")
    private String phoneNumber;

    @NotNull
    @NotBlank(message = "User type is required!")
    private String accountType;

    @NotNull()
    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String password1;

    @NotNull
    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String password2;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.trim().toLowerCase();
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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String type) {
        this.accountType = type;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    // TODO: move to a global utils library
    /**
     * Converts the given string to title case. In title case, the first letter of each word is
     * converted to uppercase, while the rest of the letters are converted to lowercase. Whitespace
     * (e.g. spaces, tabs) separates words.
     *
     * @param text The input string to be converted.
     * @return A new string in title case, or the original string if it's null or empty.
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
