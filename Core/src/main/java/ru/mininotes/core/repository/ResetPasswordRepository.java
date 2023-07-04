package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mininotes.core.domain.ResetPasswordEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordEntity, Long> {

    List<ResetPasswordEntity> findAnyByEmail(String email);

    List<ResetPasswordEntity> findAllByResetKey(String resetKey);
}
