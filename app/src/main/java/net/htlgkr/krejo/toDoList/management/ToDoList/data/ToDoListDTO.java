package net.htlgkr.krejo.toDoList.management.ToDoList.data;

import lombok.Data;

@Data
public class ToDoListDTO {
    private String name;
    private String additionalData;

    public ToDoListDTO(String name, String additionalData) {
        this.name = name;
        this.additionalData = additionalData;
    }
}
