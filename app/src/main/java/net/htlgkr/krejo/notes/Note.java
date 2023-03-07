package net.htlgkr.krejo.notes;

import java.time.LocalDateTime;

public class Note {
    private LocalDateTime localDateTime;
    private String noteContent;

    public Note(LocalDateTime localDateTime, String noteContent) {
        this.localDateTime = localDateTime;
        this.noteContent = noteContent;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
