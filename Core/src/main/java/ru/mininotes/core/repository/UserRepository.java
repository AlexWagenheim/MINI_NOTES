package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> getUserByUsername(String username);
}
