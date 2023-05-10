package net.htlgkr.krejo.toDoList.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.htlgkr.krejo.toDoList.todo.ToDo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ToDoList {
    private String name;
    private List<ToDo> toDoList;
    @JsonIgnore
    private List<ToDo> toDoListWithoutDoneTasks;

    public ToDoList() {
        name = "null";
        toDoList = new ArrayList<>();
        toDoListWithoutDoneTasks = new ArrayList<>();
    }

    public ToDoList(String name, List<ToDo> toDoList) {
        this.name = name;
        this.toDoList = toDoList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ToDo> getToDoList() {
        return toDoList;
    }

    public void setToDoList(List<ToDo> toDoList) {
        this.toDoList = toDoList;
    }

    public List<ToDo> getToDoListWithoutDoneTasks() {
        return toDoListWithoutDoneTasks;
    }

    public void syncLists() {
        toDoListWithoutDoneTasks = toDoList.stream().filter(x -> !x.getChecked()).collect(Collectors.toList());
        toDoListWithoutDoneTasks.sort(ToDo::compareTo);
    }

    public void sortLists(Comparator<ToDo> c){
        toDoList.sort(c);
        toDoListWithoutDoneTasks.sort(c);
    }
}
