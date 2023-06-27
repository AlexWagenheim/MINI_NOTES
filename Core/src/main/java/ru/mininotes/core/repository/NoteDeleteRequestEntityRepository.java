package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.NoteDeleteRequestEntity;

public interface NoteDeleteRequestEntityRepository extends JpaRepository<NoteDeleteRequestEntity, Long> {
}
