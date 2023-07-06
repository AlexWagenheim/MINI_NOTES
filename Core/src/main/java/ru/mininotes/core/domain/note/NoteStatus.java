package ru.mininotes.core.domain.note;

import ru.mininotes.core.domain.note.Note;

/**
 * статус <b>заметки</b> {@link Note}
 */
public enum NoteStatus {
    /** Заметка открыта для взаимодействия */
    ACTIVE,
    /** Заметка перемещена в корзину */
    DELETED
}
