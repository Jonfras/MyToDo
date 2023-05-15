package net.htlgkr.krejo.toDoList.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.htlgkr.krejo.toDoList.todo.ToDo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ToDoList implements Serializable, Comparable<ToDoList> {
    @Override
    public String toString() {
        return "ToDoList{" +
                "name='" + name + '\'' +
                ", toDoList=" + toDoList +
                ", toDoListWithoutDoneTasks=" + toDoListWithoutDoneTasks +
                '}';
    }

    private String name;

    private List<ToDo> toDoList;
    @JsonIgnore
    private List<ToDo> toDoListWithoutDoneTasks;
    public ToDoList(ToDoList toDoList) {
        this.name = toDoList.getName();
        this.toDoList = toDoList.getToDoList();
        syncLists();
    }

    public ToDoList() {
        name = "null";
        toDoList = new ArrayList<>();
        toDoListWithoutDoneTasks = new ArrayList<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDoList toDoList1 = (ToDoList) o;
        return Objects.equals(name, toDoList1.name) && Objects.equals(toDoList, toDoList1.toDoList) && Objects.equals(toDoListWithoutDoneTasks, toDoList1.toDoListWithoutDoneTasks);
    }

    @Override
    public int compareTo(ToDoList o) {
        return this.name.compareTo(o.getName());
    }
}
