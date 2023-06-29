package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * класс <b>Пользователь</b>
 */
@Entity
@Table(name = "Person")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @NotEmpty
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

    /** список проектов (папок) {@link Project} пользователя,
     * для которых он является владельцем
     * */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Project> projectSet = new HashSet<>();

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
        project.getEditorGroup().add(this);
        project.getModeratorGroup().add(this);
        project.getSpectatorGroup().add(this);
    }

    public void removeProject(Project project) {
        projectSet.remove(project);
        project.getEditorGroup().remove(this);
        project.getModeratorGroup().remove(this);
        project.getSpectatorGroup().remove(this);
        project.setOwner(null);
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
}
