package net.htlgkr.krejo.toDoList.management.toDo.data;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ToDoDTO {
    private int todoListId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String state;
    private String additionalData;
}
