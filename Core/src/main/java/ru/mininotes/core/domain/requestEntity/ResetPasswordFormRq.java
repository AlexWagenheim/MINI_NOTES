package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ResetPasswordFormRq {

    @NotNull
    @NotEmpty(message = "Введите пароль")
    private String password;
    @NotNull
    @NotEmpty(message = "Повторите пароль")
    private String passwordConfirm;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
