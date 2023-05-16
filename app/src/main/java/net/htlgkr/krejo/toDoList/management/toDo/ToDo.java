package net.htlgkr.krejo.toDoList.management.toDo;

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

import java.io.Serializable;
import java.time.LocalDate;

public class ToDo implements Comparable<ToDo>, Serializable {
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
    public ToDo(@JsonProperty("localDate") LocalDate localDate,
                @JsonProperty("noteContent") String noteContent,
                @JsonProperty("getChecked") boolean checked) {
        this.localDate = localDate;
        this.noteContent = noteContent;
        this.checked = false;
        setChecked(checked);
    }

    public ToDo() {
    }

    public boolean toggleChecked() {
        this.checked = !checked;
        return checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public static String serialize(ToDo toDo) throws JsonProcessingException {
        return objectMapper.writeValueAsString(toDo);
    }

    public static ToDo deserialize(String line) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(line);
        return objectMapper.treeToValue(node, ToDo.class);
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
    public int compareTo(ToDo o) {
        if (this.checked == o.checked ) {

            if (this.getLocalDate().isBefore(o.getLocalDate())) {
                return -1;

            } else if (this.getLocalDate().isAfter(o.getLocalDate())) {
                return 1;

            } else {
                return 0;

            }

        } else if (this.checked){
            return 1;
        } else {
            return -1;
        }

    }
}
