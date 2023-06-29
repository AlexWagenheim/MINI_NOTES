package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SignUpRq {
    @NotNull
    @NotEmpty(message = "Введите имя пользователя")
    private String username;
    @NotNull
    @NotEmpty(message = "Введите e-mail")
    @Email(message = "Введите корректный e-mail")
    private String email;
    @NotNull
    @NotEmpty(message = "Введите пароль")
    private String password;
    @NotNull
    @NotEmpty(message = "Повторите пароль")
    private String passwordConfirm;

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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
