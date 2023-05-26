package net.htlgkr.krejo.toDoList.management.ToDoList.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.htlgkr.krejo.toDoList.management.toDo.data.ToDo;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.*;

@Data
public class ToDoList implements Serializable, Comparable<ToDoList> {
    private String name;
    private String additionalData;
    private List<ToDo> toDoList;
    @JsonIgnore
    private List<ToDo> toDoListWithoutDoneTasks;

    public ToDoList(String name, String additionalData, List<ToDo> toDoList) {
        this.name = name;
        this.additionalData = additionalData;
        this.toDoList = toDoList;
    }

    public ToDoList() {
    }

    @Override
    public String toString() {
        return "ToDoList{" +
                "name='" + name + '\'' +
                ", toDoList=" + toDoList +
                ", toDoListWithoutDoneTasks=" + toDoListWithoutDoneTasks +
                '}';
    }

    public void syncLists() {
        toDoListWithoutDoneTasks = toDoList.stream().filter(x -> !x.isChecked()).collect(Collectors.toList());
        toDoListWithoutDoneTasks.sort(ToDo::compareTo);
    }

    public void sortLists(Comparator<ToDo> c){
        toDoList.sort(c);
        toDoListWithoutDoneTasks.sort(c);
    }



    @Override
    public int compareTo(ToDoList o) {
        return this.name.compareTo(o.getName());
    }
}
