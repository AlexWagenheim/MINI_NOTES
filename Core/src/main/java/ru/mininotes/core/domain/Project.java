package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * класс <b>Проект</b>
 */
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @NotEmpty
    /** Заголовок */
    private String title;

    @NotNull
    /** Дата и время создания */
    private Date createdDateTime;

    @NotNull
    /** Дата и время последнего изменения */
    private Date lastUpdateDateTime;

    @NotNull
    /** Является ли проект публичным?
     * true - все могут просматривать все заметки проекта
     * false - у каждой заметки есть список пользователей {@link User},
     * имеющих определённый уровень доступа к ней
     * */
    private boolean isPublic = false;

    /** Владелец (создатель) {@link User} проекта */
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @ManyToMany
    /** Список пользователей {@link User}, способных
     * просматривать/создавать/редактировать/удалять все заметки {@link Note} в проекте
     * */
    Set<User> editorGroup = new HashSet<>();

    @ManyToMany
    /** Список пользователей {@link User}, способных
     * просматривать/редактировать все заметки {@link Note} в проекте
     * */
    Set<User> moderatorGroup = new HashSet<>();

    @ManyToMany
    /** Список пользователей {@link User}, способных
     * просматривать все заметки {@link Note} в проекте
     * */
    Set<User> spectatorGroup = new HashSet<>();

    /** Список заметок {@link Note} в проекте */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Note> noteSet = new HashSet<>();

    public Project() {
    }

    public void addNote(Note note) {
        noteSet.add(note);
        note.setProject(this);
        this.lastUpdateDateTime = new Date();
    }

    public void removeNote(Note note) {
        noteSet.remove(note);
        note.setProject(null);
        this.lastUpdateDateTime = new Date();
    }

    public Project(String title, Date createdDateTime, boolean isPublic) {
        this.title = title;
        this.createdDateTime = createdDateTime;
        this.lastUpdateDateTime = createdDateTime;
        this.isPublic = isPublic;
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
