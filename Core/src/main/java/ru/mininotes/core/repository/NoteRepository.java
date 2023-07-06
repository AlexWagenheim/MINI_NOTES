package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.note.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
