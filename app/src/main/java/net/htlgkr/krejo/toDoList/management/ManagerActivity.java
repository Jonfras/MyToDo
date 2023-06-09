package net.htlgkr.krejo.toDoList.management;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import net.htlgkr.krejo.toDoList.ConstantsMyToDo;
import net.htlgkr.krejo.toDoList.HTTPSHelper;
import net.htlgkr.krejo.toDoList.Message;
import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.login.user.User;
import net.htlgkr.krejo.toDoList.management.ToDoList.ToDoListDataService;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoList;
import net.htlgkr.krejo.toDoList.management.ToDoList.ToDoListActivity;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoListDTO;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoListResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ManagerActivity extends AppCompatActivity {
    private static final int RQ_WRITE_STORAGE = 2;
    private static List<ToDoList> managerList = new ArrayList<>();
    private static final String FILENAME = "toDoLists.json";
    private static final String SINGLE_LIST = "recentToDoList.json";
    private View createToDoListView;
    private EditText editTextNameOfToDoList;
    private Button okButton;
    private Button cancelButton;
    private ListView listView;
    private ManagerAdapter managerAdapter;
    private static ToDoList selectedToDoList;
    private User user;
    private boolean networkAvailable;
    private static final HTTPSHelper httpsHelper = new HTTPSHelper();

    private static List<ToDoListResource> allToDoListRessourcesFromServer = new ArrayList<>();

    private ToDoListDataService toDoListDataService = new ToDoListDataService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_manager);

        getObjectsFromIntent();
        setUpViews();
        setUpManagerList();

        if (selectedToDoList != null) {
            try {
                editManagerListOnRecentEdit();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onCreate: " + SINGLE_LIST + " wasn't found");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        listViewOnItemClicklistener();
    }

    private void getObjectsFromIntent() {
        Bundle bundle = getIntent().getExtras();
        user = (User) bundle.get("user");
        networkAvailable = (boolean) bundle.get("networkAvailable");
    }


    private void editManagerListOnRecentEdit() throws
            FileNotFoundException, JsonProcessingException {
        ToDoList editedToDoList = readRecentToDoList(getInputStream(SINGLE_LIST));
        if (managerList.size() > 0) {
            List<ToDoList> tempList = managerList.stream()
                    .filter(x -> x.getName()
                            .equals(editedToDoList.getName()))
                    .collect(Collectors.toList());

            if (!tempList.isEmpty()) {
                managerList.get(managerList.indexOf(tempList.get(0)))
                        .setToDoList(editedToDoList.getToDoList());
            }
        }
        clearRecentToDoFile();
    }

    private void clearRecentToDoFile() {
        String path = getFilesDir().getAbsolutePath() + File.separator + SINGLE_LIST;
        System.out.println(new File(path).delete());
    }

    private ToDoList readRecentToDoList(FileInputStream inputStream) throws
            JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Scanner fileScanner = new Scanner(inputStream);
        return objectMapper.readValue(fileScanner.nextLine(), ToDoList.class);
    }

    private void listViewOnItemClicklistener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                selectedToDoList = managerAdapter.getToDoLists().get(position);
                startNewActivity(view, position, id);
            } catch (IOException e) {
                Log.e(TAG, "listViewOnItemClicklistener:" +
                        " todoList couldn't be saved before switching Activity");
                throw new RuntimeException(e);
            }
        });
    }

    private void setUpManagerList() {
        if (networkAvailable) {
            getToDoListRessourcesFromServerAsync();
            filterRelevantLists();
            managerList = toDoListDataService
                    .convertToDoListResourceListToToDoListEntityList(allToDoListRessourcesFromServer);
        } else {
            try {
                managerList = readManagerJSON(getInputStream(FILENAME));
            } catch (FileNotFoundException e) {
                Toast.makeText(ManagerActivity.this, "No ToDoListsFound in " + FILENAME, Toast.LENGTH_SHORT).show();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        managerAdapter = new ManagerAdapter(managerList, R.layout.list_item_manager, ManagerActivity.this);
        listView.setAdapter(managerAdapter);
        managerAdapter.notifyDataSetChanged();
    }

    private void getToDoListRessourcesFromServerAsync() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Integer, Void, List<ToDoListResource>> asyncTask
                = new AsyncTask<Integer, Void, List<ToDoListResource>>() {
            @Override
            protected List<ToDoListResource> doInBackground(Integer... integers) {
                try {
                    allToDoListRessourcesFromServer = (List<ToDoListResource>) httpsHelper.sendRequest(ConstantsMyToDo.TODOLISTS
                                    + ConstantsMyToDo.USERNAME + "=" + user.getUsername()
                                    + "&"
                                    + ConstantsMyToDo.PASSWORD + "=" + user.getPassword(),
                            ConstantsMyToDo.GET,
                            Optional.empty(),
                            allToDoListRessourcesFromServer
                    );

                } catch (IOException e) {
                    allToDoListRessourcesFromServer = null;
                    Log.e(TAG, "doInBackground: Todolists couldn't be downloaded");
                    throw new RuntimeException(e);
                }
                return allToDoListRessourcesFromServer;
            }

            @Override
            protected void onPostExecute(List<ToDoListResource> serverManagerList) {
                if (allToDoListRessourcesFromServer == null || allToDoListRessourcesFromServer.isEmpty()) {
                    Log.e(TAG, "onPostExecute: No toDoLists were found");
                } else {
                    System.out.println("The following todolists were found:");
                    allToDoListRessourcesFromServer.forEach(System.out::println);
                }
            }
        };
        asyncTask.execute();
    }

    private List<ToDoList> filterRelevantLists() {
        int userId = user.getUserId();
        List<ToDoListResource> relevantResources = allToDoListRessourcesFromServer
                .stream()
                .filter(toDoList -> toDoList.getOwnerId() == userId)
                .collect(Collectors.toList());
        return toDoListDataService.convertToDoListResourceListToToDoListEntityList(relevantResources);
    }

    private void setUpViews() {
        listView = findViewById(R.id.managerListView);
        registerForContextMenu(listView);

        createToDoListView = getLayoutInflater().inflate(R.layout.dialog_create_to_do_list, null);
        editTextNameOfToDoList = createToDoListView.findViewById(R.id.editTextToDoListName);
        okButton = createToDoListView.findViewById(R.id.addToDoListOkButton);
        cancelButton = createToDoListView.findViewById(R.id.addToDoListCancelButton);
    }

    public void startNewActivity(View view, int position, long id)
            throws IOException {

        handleSaveToDoLists();

        ToDoList toDoList = new ToDoList();
        toDoList.setName(managerList.get(position).getName());
        toDoList.setToDoList(managerList.get(position).getToDoList());

        Intent intent = new Intent(this, ToDoListActivity.class);
        intent.putExtra("toDoList", toDoList);
        startActivity(intent);
    }

    private List<ToDoList> readManagerJSON(FileInputStream fileInputStream) throws
            JsonProcessingException {
        List newManagerList = new ArrayList<>();

        Scanner fileScanner = new Scanner(fileInputStream);
        StringBuilder json = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();

        while (fileScanner.hasNext()) {
            json.append(fileScanner.nextLine());

        }
        if (json.toString().isEmpty()) {
            System.out.println("no todoLists found");
            new AlertDialog.Builder(this)
                    .setMessage("No ToDoLists found in file")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            newManagerList = objectMapper.readValue(String.valueOf(json), objectMapper.getTypeFactory().constructCollectionType(List.class, ToDoList.class));
        }

        return newManagerList;
    }

    private FileInputStream getInputStream(String filename) throws FileNotFoundException {
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(filename);
        } catch (FileNotFoundException e) {
            System.err.println("File was not found");
            throw new FileNotFoundException();
        }
        return fileInputStream;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addToDoList:
                handleCreateToDoList(false);
                break;
            case R.id.saveToDoList:
                try {
                    handleSaveToDoLists();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return (super.onOptionsItemSelected(item));
    }

    private void handleCreateToDoList(boolean edit) {
        if (createToDoListView.getParent() != null) {
            ((ViewGroup) createToDoListView.getParent()).removeView(createToDoListView);
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setView(createToDoListView);

        AlertDialog createToDoListDialog = dialogBuilder.create();

        okButton.setOnClickListener(v -> {
            ToDoList toDoList = new ToDoList();

            if (editTextNameOfToDoList.getText().toString().equals("")) {
                new AlertDialog.Builder(this)
                        .setMessage("Define a name for your ToDoList!")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                toDoList.setName(editTextNameOfToDoList.getText().toString());
                if (edit) {
                    toDoList.setToDoList(selectedToDoList.getToDoList());
                    managerList.set(managerList.indexOf(selectedToDoList), toDoList);
                    editToDoListOnServerAsync(toDoList);
                } else {
                    toDoList.setToDoList(new ArrayList<>());
                    managerList.add(toDoList);
                }
                managerAdapter.notifyDataSetChanged();

                createToDoListDialog.cancel();
            }
        });

        cancelButton.setOnClickListener(v -> {
            createToDoListDialog.cancel();
        });

        editTextNameOfToDoList.setText("");

        createToDoListDialog.show();
    }

    private void editToDoListOnServerAsync(ToDoList selectedToDoList) {
        @SuppressLint("StaticFieldLeak") AsyncTask<ToDoList, Void, ToDoListResource> asyncTask
                = new AsyncTask<ToDoList, Void, ToDoListResource>() {
            @Override
            protected ToDoListResource doInBackground(ToDoList... toDoLists) {
                System.out.println(toDoLists[0]);
                ToDoList toDoList = toDoLists[0];
                ToDoListResource toDoListResource;
                ToDoListDTO toDoListDTO = toDoListDataService.convertToDoListEntityToToDoListDTO(toDoList);
                try {
                    toDoListResource = ((ToDoListResource) httpsHelper.sendRequest(
                            ConstantsMyToDo.TODOLISTS
                                    + ConstantsMyToDo.ID + "=" + toDoList.getId()
                                    + "&"
                                    + ConstantsMyToDo.USERNAME + "=" + user.getUsername()
                                    + "&"
                                    + ConstantsMyToDo.PASSWORD + "=" + user.getPassword()
                            ,
                            ConstantsMyToDo.PUT,
                            Optional.of(toDoListDTO),
                            new ToDoListResource()));

                } catch (
                        IOException e) {
                    Log.e(TAG, "doInBackground: ToDoList couldn't be sent to server");
                    toDoListResource = null;
                }
                return toDoListResource;
            }

            @Override
            protected void onPostExecute(ToDoListResource toDoListResource) {

                if (toDoListResource == null) {
                    Log.e(TAG, "onPostExecute: No TodoList was edited");
                } else {
                    System.out.println("The following todolist was updated:");
                    System.out.println(toDoListResource);
                }
            }
        };
        asyncTask.execute(selectedToDoList);
    }

    private void handleSaveToDoLists() throws IOException {
        if (networkAvailable) {
            if (!managerList.isEmpty()) {
                addToDoListToServerAsync(managerList.toArray(new ToDoList[0]));
            } else {
                Log.e(TAG, "No ToDoLists to save.");
            }
        } else {
            writeToDoListsToFile();
        }
    }

    private void addToDoListToServerAsync(ToDoList... toDoLists) {
        @SuppressLint("StaticFieldLeak") AsyncTask<ToDoList, Void, List<ToDoListResource>> asyncTask
                = new AsyncTask<ToDoList, Void, List<ToDoListResource>>() {
            @Override
            protected List<ToDoListResource> doInBackground(ToDoList... toDoLists) {
                System.out.println(toDoLists[0]);
                List<ToDoListResource> resourceList = new ArrayList<>();
                try {
                    for (ToDoListDTO toDoListDTO : toDoListDataService.
                            convertToDoListEntityListToToDoListDTOList(
                                    Arrays.asList(toDoLists))) {

                        resourceList.add((ToDoListResource) httpsHelper.sendRequest(
                                ConstantsMyToDo.TODOLISTS
                                        + ConstantsMyToDo.USERNAME + "=" + user.getUsername()
                                        + "&"
                                        + ConstantsMyToDo.PASSWORD + "=" + user.getPassword(),
                                ConstantsMyToDo.POST,
                                Optional.of(toDoListDTO),
                                new ToDoListResource()));
                    }

                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ToDoLists couldn't be sent to server");
                    resourceList = null;
                }
                return resourceList;
            }

            @Override
            protected void onPostExecute(List<ToDoListResource> resourceList) {

                if (resourceList.isEmpty()) {
                    Log.e(TAG, "onPostExecute: No TodoList was uploaded");
                } else {
                    System.out.println("The following todolists were uploaded:");
                    // TODO: 05.06.2023 moch do so dass olle todoListen in deina app vo de resourcen aktualisiert werden damit ma a id hod. 
                    resourceList.forEach(System.out::println);
                }
            }
        };
        asyncTask.execute(toDoLists);
    }

    private void writeToDoListsToFile() throws JsonProcessingException {
        FileOutputStream fos = null;
        Toast.makeText(ManagerActivity.this, "Saving...", Toast.LENGTH_SHORT).show();

        try {
            fos = openFileOutput(FILENAME, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fos, true);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(managerList);
        pw.println(jsonString);

        pw.flush();
        pw.close();
        try {
            fos.close();
        } catch (IOException e) {
            System.err.println("fos couldn't be closed");
        }

        Toast.makeText(ManagerActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu_manager, menu);

        selectedToDoList = managerAdapter.getToDoLists().get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item: {
                handleEdit();
                break;
            }
            case R.id.delete_item: {
                handleDelete();
                break;
            }
            default:
                return false;
        }
        return false;
    }

    private void handleDelete() {
        managerList.remove(selectedToDoList);
        removeToDoListFromServerAsync(selectedToDoList);
        managerAdapter.notifyDataSetChanged();
    }

    private void removeToDoListFromServerAsync(ToDoList selectedToDoList) {
        @SuppressLint("StaticFieldLeak") AsyncTask<ToDoList, Void, Message> asyncTask
                = new AsyncTask<ToDoList, Void, Message>() {
            @Override
            protected Message doInBackground(ToDoList... toDoLists) {
                System.out.println(toDoLists[0]);
                int idOfToDoList = toDoLists[0].getId();
                Message response = new Message();
                try {
                    response = (Message) httpsHelper.sendRequest(
                            ConstantsMyToDo.TODOLISTS
                                    + ConstantsMyToDo.ID + "=" + idOfToDoList
                                    + "&"
                                    + ConstantsMyToDo.USERNAME + "=" + user.getUsername()
                                    + "&"
                                    + ConstantsMyToDo.PASSWORD + "=" + user.getPassword()
                            ,
                            ConstantsMyToDo.PUT,
                            Optional.empty(),
                            Optional.of(new Message()));

                } catch (
                        IOException e) {
                    Log.e(TAG, "doInBackground: ToDoList couldn't be sent to server");
                }
                return response;
            }

            @Override
            protected void onPostExecute(Message message) {
                if (message == null) {
                    Log.e(TAG, "onPostExecute: todolist was deleted");
                } else {
                    Log.e(TAG, "onPostExecute: " + message.getMessage() );
                }
            }
        };
        asyncTask.execute(selectedToDoList);
    }

    private void handleEdit() {
        editTextNameOfToDoList.setText(managerList.get(managerList.indexOf(selectedToDoList)).getName());
        handleCreateToDoList(true);
    }


}
