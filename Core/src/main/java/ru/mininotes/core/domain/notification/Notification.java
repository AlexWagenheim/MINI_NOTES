package ru.mininotes.core.domain.notification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.mininotes.core.domain.user.User;

import java.util.Date;

@Entity
public class Notification {

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
    private String content;

    @NotNull
    private boolean hasRead;

    @NotNull
    /** Дата и время создания */
    private Date createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public Notification() {
    }

    public Notification(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.createdDateTime = new Date();
        this.hasRead = false;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isHasRead() {
        return hasRead;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
}
