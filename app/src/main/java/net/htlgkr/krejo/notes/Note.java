package net.htlgkr.krejo.notes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;

public class Note implements Comparable<Note> {


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate localDate;

    @JsonProperty("noteContent")
    private String noteContent;

    @JsonProperty("getChecked")
    private boolean checked;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @JsonCreator
    public Note(@JsonProperty("localDate") LocalDate localDate,
                @JsonProperty("noteContent") String noteContent,
                @JsonProperty("getChecked") boolean checked) {
        this.localDate = localDate;
        this.noteContent = noteContent;
        this.checked = checked;
    }

    public Note() {
    }

    public boolean toggleChecked(){
        this.checked = !checked;
        return checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public static String serialize(Note note) throws JsonProcessingException {
        return objectMapper.writeValueAsString(note);
    }

    public static Note deserialize(String line) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(line);
        return objectMapper.treeToValue(node, Note.class);
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
        if (this.getLocalDate().isBefore(o.getLocalDate())) {

            return -1;

        } else if (this.getLocalDate().isAfter(o.getLocalDate())) {

            return 1;

        }

        return 0;
    }
}
