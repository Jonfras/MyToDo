package net.htlgkr.krejo.toDoList.management.ToDoList;

import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoList;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoListDTO;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoListResource;

import java.util.ArrayList;
import java.util.List;

public class ToDoListDataService {

    public ToDoList convertToDoListResourceToToDoListEntity(ToDoListResource resource) {
        return new ToDoList(
                resource.getName(),
                resource.getAdditionalData(),
                null);
    }

    public List<ToDoList> convertToDoListResourceListToToDoListEntityList(List<ToDoListResource> resourceList) {
        List<ToDoList> toDoLists = new ArrayList<>();
        for (ToDoListResource resource : resourceList) {
            toDoLists.add(convertToDoListResourceToToDoListEntity(resource));
        }
        return toDoLists;
    }

    public ToDoListDTO convertToDoListEntityToToDoListDTO(ToDoList toDoList){
        return new ToDoListDTO(toDoList.getName(), toDoList.getAdditionalData());
    }

    public List<ToDoListDTO> convertToDoListEntityListToToDoListDTOList(List<ToDoList> toDoLists) {
        List<ToDoListDTO> toDoListDTOS = new ArrayList<>();
        for (ToDoList toDoList :
                toDoLists) {
            toDoListDTOS.add(convertToDoListEntityToToDoListDTO(toDoList));
        }
        return toDoListDTOS;
    }
}
