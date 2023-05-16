package net.htlgkr.krejo.toDoList.management.ToDoList.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.htlgkr.krejo.toDoList.management.toDo.ToDo;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.*;

@Data
public class ToDoList implements Serializable, Comparable<ToDoList> {
    private String name;
    private String additionalData;
    private List<ToDo> toDoList;
    @JsonIgnore
    private List<ToDo> toDoListWithoutDoneTasks;

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
