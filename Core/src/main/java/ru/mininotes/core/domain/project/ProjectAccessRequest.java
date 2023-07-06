package ru.mininotes.core.domain.project;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
public class ProjectAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private boolean isActive;

    private ProjectAccessRequestType requestType;

    @NotNull
    @NotEmpty
    private String username;

    @NotNull
    /** Дата и время создания */
    private Date createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    public ProjectAccessRequest() {
    }

    public ProjectAccessRequest(ProjectAccessRequestType requestType, String username) {
        this.requestType = requestType;
        this.username = username;
        this.isActive = true;
        this.createdDateTime = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ProjectAccessRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ProjectAccessRequestType requestType) {
        this.requestType = requestType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
