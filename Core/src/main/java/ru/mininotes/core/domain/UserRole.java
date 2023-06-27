package ru.mininotes.core.domain;

/**
 * доступные роли <b>пользователя</b> {@link User}
 */
public enum UserRole {
    /** Авторизованный пользователь */
    USER,
    /** Авторизованный пользователь с правами администратора */
    ADMIN
}
