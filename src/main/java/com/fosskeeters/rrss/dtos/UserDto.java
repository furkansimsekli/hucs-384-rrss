package com.fosskeeters.rrss.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {
    @Size(min = 1, max = 64, message = "First name must be between 1-64 characters!")
    private String firstName;

    @Size(min = 1, max = 64, message = "Last name must be between 1-64 characters!")
    private String lastName;

    @Size(min = 1, max = 64, message = "Username must be between 1-16 characters!")
    private String username;

    @Size(min = 5, max = 160, message = "Email address must be between 5-160 characters!")
    @Email
    private String email;

    @Size(min = 1, max = 64, message = "Phone number must be between 1-15 characters!")
    private String phoneNumber;

    // FIXME: constrain to hold only "Customer" and "Merchant"
    @NotBlank(message = "User type is required!")
    private String type;

    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String password1;

    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String password2;

    @AssertTrue(message = "Passwords do not match!")
    private boolean doesPasswordMatch() {
        return password1.equals(password2);
    }

    @AssertTrue
    private boolean isTypeValid() {
        return type.equals("Customer") || type.equals("Merchant");
    }
}
