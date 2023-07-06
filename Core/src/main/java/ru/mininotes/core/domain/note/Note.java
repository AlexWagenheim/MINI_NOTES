package ru.mininotes.core.domain.note;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.mininotes.core.domain.user.User;
import ru.mininotes.core.domain.project.Project;

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
    /** Основной текст в формате markdown */
    private String content;

    @NotEmpty
    @NotNull
    @Column(columnDefinition="TEXT")
    /** Основной текст в формате html */
    private String html;

    @NotNull
    /** Дата и время создания */
    private Date createdDateTime;

    @NotNull
    /** Дата и время последнего изменения */
    private Date lastUpdateDateTime;

    /** Статус {@link NoteStatus} заметки */
    private NoteStatus status;

    /** Проект {@link Project}, которому принадлежит заметка */
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    public Note() {
    }

    public boolean canEdit(Project project, User user) {
        return this.status.equals(NoteStatus.ACTIVE) && this.project.equals(project) && this.project.canEditNote(user);
    }

    public boolean canDelete(Project project, User user) {
        return this.status.equals(NoteStatus.ACTIVE) && this.project.equals(project) && this.project.canDeleteNote(user);
    }

    public boolean canView(Project project, User user) {
        return this.status.equals(NoteStatus.ACTIVE) && this.project.equals(project) && this.project.canViewNote(user);
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}
