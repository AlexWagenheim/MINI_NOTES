package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ProjectRq {
    @NotNull
    @NotEmpty(message = "Название не должно быть пустым")
    private String title;

    @NotNull
    @NotEmpty
    private String status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String aPublic) {
        status = aPublic;
    }
}
