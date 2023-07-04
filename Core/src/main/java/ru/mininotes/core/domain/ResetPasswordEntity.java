package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Calendar;
import java.util.Date;

@Entity
public class ResetPasswordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @NotEmpty
    private String resetKey;

    @NotNull
    @NotEmpty
    @Email
    private String email;

    @NotNull
    private Date expiredTime;

    @NotNull
    private boolean isActive;

    public ResetPasswordEntity() {
    }

    public ResetPasswordEntity(String resetKey, String email) {
        this.resetKey = resetKey;
        this.email = email;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        this.expiredTime = calendar.getTime();
        this.isActive = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
