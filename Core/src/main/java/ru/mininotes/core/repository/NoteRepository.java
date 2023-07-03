package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mininotes.core.domain.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
