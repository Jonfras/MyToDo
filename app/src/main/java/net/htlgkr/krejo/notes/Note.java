package net.htlgkr.krejo.notes;

import java.time.LocalDate;

public class Note implements Comparable<Note>{
    public static final String REGEX = ":";
    private LocalDate localDate;
    private String noteContent;
    private boolean checked;


    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }


    public Note(LocalDate localDate, String noteContent, boolean isChecked) {
        this.localDate = localDate;
        this.noteContent = noteContent;
        this.checked = isChecked;
    }

    public static String serialize(Note note){
        return note.getLocalDate() + REGEX + note.getNoteContent() + REGEX + note.isChecked();
    }

    public static Note deserialize(String line) {
        String[] arr = line.split(REGEX);

        boolean checked = false;

        if (arr[2].equals("true")){
            checked = true;

        }

       return new Note(LocalDate.parse(line.split(REGEX)[0]), line.split(REGEX)[1], checked);
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


    @Override
    public int compareTo(Note o) {
        if (this.getLocalDate().isBefore(o.getLocalDate())){

            return -1;

        } else if (this.getLocalDate().isAfter(o.getLocalDate())){

            return 1;

        }

        return 0;
    }
}
