package net.htlgkr.krejo.toDoList.management.ToDoList.data;

import lombok.Data;

@Data
public class ToDoListResource {
    private int id;
    private int ownerId;
    private String name;
    private String additionalData;

    public ToDoListResource(int id, int ownerId, String name, String additionalData) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.additionalData = additionalData;
    }

    public ToDoListResource() {

    }
}
