package net.htlgkr.krejo.toDoList.management.ToDoList;

import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoList;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoListResource;

import java.util.ArrayList;
import java.util.List;

public class ToDoListDataService {

    public static List<ToDoList> convertToDoListResourceToToDoListEntity(List<ToDoListResource> resourceList){
        List<ToDoList> managerList = new ArrayList<>();
        for (ToDoListResource resource : resourceList) {
            managerList.add(new ToDoList(
                    resource.getName(),
                    resource.getAdditionalData(),
                    null));
        }

        return managerList;
    }
}
