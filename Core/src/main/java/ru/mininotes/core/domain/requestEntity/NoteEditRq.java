package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class NoteEditRq {

    @NotNull
    @NotEmpty(message = "Название не может быть пустым")
    private String title;

    @NotNull
    @NotEmpty(message = "Текст заметки не должен быть пустым")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
