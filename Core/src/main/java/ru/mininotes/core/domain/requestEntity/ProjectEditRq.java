package ru.mininotes.core.domain.requestEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ProjectEditRq {
    @NotNull
    @NotEmpty(message = "Название не должно быть пустым")
    private String title;

    @NotNull
    @NotEmpty
    private String status;

    private Set<String> editorGroup = new HashSet<>();

    private Set<String> moderatorGroup = new HashSet<>();

    private Set<String> spectatorGroup = new HashSet<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getEditorGroup() {
        return editorGroup;
    }

    public void setEditorGroup(Set<String> editorGroup) {
        this.editorGroup = editorGroup;
    }

    public Set<String> getModeratorGroup() {
        return moderatorGroup;
    }

    public void setModeratorGroup(Set<String> moderatorGroup) {
        this.moderatorGroup = moderatorGroup;
    }

    public Set<String> getSpectatorGroup() {
        return spectatorGroup;
    }

    public void setSpectatorGroup(Set<String> spectatorGroup) {
        this.spectatorGroup = spectatorGroup;
    }
}
