package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ResetPasswordRq {

    @NotNull
    @NotEmpty(message = "Поле не должно быть пустым")
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
