package ru.mininotes.core.domain.user;

/**
 * статус учётной записи <b>пользователя</b> {@link User}
 */
public enum UserStatus {
    /** Пользователь подтвердил свой e-mail адрес и не заблокирован */
    ACTIVE,
    /** Пользователь заблокирован */
    BANNED,
    /** Пользователь не заблокирован, но ещё не подтвердил свой e-mail адрес */
    NOT_CONFIRMED
}
