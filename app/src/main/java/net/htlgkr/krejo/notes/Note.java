package net.htlgkr.krejo.notes;

import java.time.LocalDate;

public class Note {
    public static final String REGEX = ":";
    private LocalDate localDate;
    private String noteContent;

    public Note(LocalDate localDate, String noteContent) {
        this.localDate = localDate;
        this.noteContent = noteContent;
    }

    public static String serialize(Note note){
        return note.getLocalDate() + ":" + note.getNoteContent();
    }

    public static Note deserialize(String line) {
       return new Note(LocalDate.parse(line.split(REGEX)[0]), line.split(REGEX)[1]);
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
