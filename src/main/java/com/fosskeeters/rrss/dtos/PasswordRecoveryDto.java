package com.fosskeeters.rrss.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PasswordRecoveryDto {
    @NotNull()
    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String newPassword1;

    @NotNull
    @Size(min = 1, max = 256, message = "Password length must be between 1-256 characters!")
    private String newPassword2;

    public String getNewPassword1() {
        return newPassword1;
    }

    public void setNewPassword1(String newPassword1) {
        this.newPassword1 = newPassword1;
    }

    public String getNewPassword2() {
        return newPassword2;
    }

    public void setNewPassword2(String newPassword2) {
        this.newPassword2 = newPassword2;
    }
}
