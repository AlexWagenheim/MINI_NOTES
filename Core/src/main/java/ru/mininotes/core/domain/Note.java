package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * класс <b>Заметка</b>
 */
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty
    @NotNull
    /** Заголовок */
    private String title;

    @NotEmpty
    @NotNull
    @Column(columnDefinition="TEXT")
    /** Основной текст */
    private String body;

    @NotEmpty
    @NotNull
    /** Дата и время создания */
    private Date createdDateTime;

    @NotEmpty
    @NotNull
    /** Дата и время последнего изменения */
    private Date lastUpdateDateTime;

    @NotEmpty
    @NotNull
    /** Статус {@link NoteStatus} заметки */
    private NoteStatus status;
    /** Проект {@link Project}, которому принадлежит заметка */
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    public Note() {
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(Date lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    public NoteStatus getStatus() {
        return status;
    }

    public void setStatus(NoteStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
