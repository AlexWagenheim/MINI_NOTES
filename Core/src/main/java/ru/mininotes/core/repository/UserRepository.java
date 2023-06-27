package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
