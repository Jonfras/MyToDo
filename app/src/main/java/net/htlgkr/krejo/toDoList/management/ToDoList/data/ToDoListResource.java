package net.htlgkr.krejo.toDoList.management.ToDoList.data;

import lombok.Data;

@Data
public class ToDoListResource {
    private int id;
    private int ownerId;
    private String name;
    private String additionalData;
}
