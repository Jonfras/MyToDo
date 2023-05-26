package net.htlgkr.krejo.toDoList.management.toDo.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class ToDoRessource {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    private int id;
    private int ownerId;
    private int todoListId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String state;
    private String additionalData;
}
