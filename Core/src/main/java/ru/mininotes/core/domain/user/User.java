package ru.mininotes.core.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.mininotes.core.domain.note.Note;
import ru.mininotes.core.domain.notification.Notification;
import ru.mininotes.core.domain.project.Project;

import java.util.*;
import java.util.stream.Collectors;

/**
 * класс <b>Пользователь</b>
 */
@Indexed
@Entity
@Table(name = "Person")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @NotEmpty
    @FullTextField()
    /** Имя пользователя */
    private String username;

    @NotNull
    @NotEmpty
    @Email
    /** e-mail пользователя */
    private String email;

    @NotNull
    @NotEmpty
    /** зашифрованный пароль пользователя */
    private String password;

    /** статус {@link UserStatus} пользователя */
    private UserStatus status;

    /** роль (уровень доступа) {@link UserRole} пользователя */
    private UserRole role;

    @NotNull
    /** имеет ли пользователь непрочитанные уведомления */
    private boolean hasNewNotification;

    /** список проектов (папок) {@link Project} пользователя,
     * для которых он является владельцем
     * */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Project> projectSet = new HashSet<>();

    /** список удалённых заметок {@link Note} пользователя */
    @OneToMany()
    Set<Note> deletedNoteSet = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Notification> notificationList = new ArrayList<>();

    public User() {
    }

    public User(String username, String email, String password, UserStatus status, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.role = role;
    }

    public void addProject(Project project) {
        projectSet.add(project);
        project.setOwner(this);
    }

    public void removeProject(Project project) {
        deletedNoteSet.removeAll(project.getNoteSet());
        projectSet.remove(project);
        project.setOwner(null);
    }

    public User getRelativeView(User user) {
        User view = new User();

        view.setId(this.id);
        view.setRole(this.role);
        view.setEmail(this.email);
        view.setUsername(this.username);
        view.setPassword(this.password);
        view.setStatus(this.status);
        view.setProjectSet(this.projectSet.stream().filter(project -> project.canView(user)).collect(Collectors.toSet()));
        view.setProjectSet(view.getProjectSet().stream().map(project -> project.getRelativeView(user)).collect(Collectors.toSet()));

        return view;
    }

    public void addNotification(Notification notification) {
        notification.setHasRead(false);
        notificationList.add(notification);
        notification.setUser(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<Project> getProjectSet() {
        return projectSet;
    }

    public void setProjectSet(Set<Project> projectSet) {
        this.projectSet = projectSet;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Set<Note> getDeletedNoteSet() {
        return deletedNoteSet;
    }

    public void setDeletedNoteSet(Set<Note> deletedNoteSet) {
        this.deletedNoteSet = deletedNoteSet;
    }

    public List<Notification> getNotificationList() {
        return notificationList;
    }

    public void setNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public boolean isHasNewNotification() {
        return hasNewNotification;
    }

    public void setHasNewNotification(boolean hasNewNotification) {
        this.hasNewNotification = hasNewNotification;
    }

}
