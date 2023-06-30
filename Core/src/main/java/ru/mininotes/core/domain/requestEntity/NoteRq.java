package ru.mininotes.core.domain.requestEntity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class NoteRq {

    @NotNull
    @NotEmpty(message = "Название не может быть пустым")
    private String title;

    @NotNull
    @NotEmpty
    private String body;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
