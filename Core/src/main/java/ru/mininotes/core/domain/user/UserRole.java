package ru.mininotes.core.domain.user;

/**
 * доступные роли <b>пользователя</b> {@link User}
 */
public enum UserRole {
    /** Авторизованный пользователь */
    ROLE_USER,
    /** Авторизованный пользователь с правами администратора */
    ROLE_ADMIN
}
