package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty
    @NotEmpty
    private String title;

    @NotEmpty
    @NotEmpty
    private Date createdDateTime;

    @NotEmpty
    @NotEmpty
    private boolean isPublic = false;

    @NotEmpty
    @NotEmpty
    private Date lastUpdateDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @OneToMany
    Set<User> editorGroup = new HashSet<>();
    @OneToMany
    Set<User> moderatorGroup = new HashSet<>();
    @OneToMany
    Set<User> spectatorGroup = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Note> noteSet = new HashSet<>();

    public Project() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Date getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(Date lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getEditorGroup() {
        return editorGroup;
    }

    public void setEditorGroup(Set<User> editorGroup) {
        this.editorGroup = editorGroup;
    }

    public Set<User> getModeratorGroup() {
        return moderatorGroup;
    }

    public void setModeratorGroup(Set<User> moderatorGroup) {
        this.moderatorGroup = moderatorGroup;
    }

    public Set<User> getSpectatorGroup() {
        return spectatorGroup;
    }

    public void setSpectatorGroup(Set<User> spectatorGroup) {
        this.spectatorGroup = spectatorGroup;
    }

    public Set<Note> getNoteSet() {
        return noteSet;
    }

    public void setNoteSet(Set<Note> noteSet) {
        this.noteSet = noteSet;
    }
}
