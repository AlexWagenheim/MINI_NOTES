package ru.mininotes.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.Date;

/**
 * класс формирующий список заметок {@link Note} на удаление по истечению времени
 */
@Entity
public class NoteDeleteRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty
    @NotEmpty
    /** Дата и время, когда заметка должна быть удалена (без возможности удаления) */
    private Date deleteDateTime;

    @OneToOne
    /** Указание на заметку для удаления */
    private Note note;

    public NoteDeleteRequestEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDeleteDateTime() {
        return deleteDateTime;
    }

    public void setDeleteDateTime(Date deleteDateTime) {
        this.deleteDateTime = deleteDateTime;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
