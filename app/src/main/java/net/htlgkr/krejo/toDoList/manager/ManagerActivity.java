package net.htlgkr.krejo.toDoList.manager;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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


import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.todo.ToDoListActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


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

    public void printInput(View view) {
        Log.d(TAG, "”entered printInput”");
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // RQ WRITE STORAGE ist just any constant value to identify the request
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RQ_WRITE_STORAGE);
        } else {
            System.out.println("woow");
        }
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
                managerList.get( managerList.indexOf( tempList.get(0) ))
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
            } catch (JsonProcessingException e) {
                Log.e(TAG, "listViewOnItemClicklistener:" +
                        " todoList couldn't be saved before switching Activity");
                throw new RuntimeException(e);
            }
        });
    }

    private void setUpManagerList() {
        try {
            managerList = readManagerJSON(getInputStream(FILENAME));
        } catch (FileNotFoundException e) {
            Toast.makeText(ManagerActivity.this, "No ToDoListsFound in " + FILENAME, Toast.LENGTH_SHORT).show();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            managerAdapter = new ManagerAdapter(managerList, R.layout.manager_list_item_layout, ManagerActivity.this);
            listView.setAdapter(managerAdapter);
            managerAdapter.notifyDataSetChanged();
        }
    }

    private void setUpViews() {
        listView = findViewById(R.id.managerListView);
        registerForContextMenu(listView);

        createToDoListView = getLayoutInflater().inflate(R.layout.create_to_do_list_dialogue, null);
        editTextNameOfToDoList = createToDoListView.findViewById(R.id.editTextToDoListName);
        okButton = createToDoListView.findViewById(R.id.addToDoListOkButton);
        cancelButton = createToDoListView.findViewById(R.id.addToDoListCancelButton);
    }

    public void startNewActivity(View view, int position, long id) throws
            JsonProcessingException {
        handleSaveToDoLists();
        Intent intent = new Intent(this, ToDoListActivity.class);
        intent.putExtra("toDoList", new ToDoList(managerList.get(position)));
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                } catch (JsonProcessingException e) {
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
                } else {
                    toDoList.setToDoList(new ArrayList<>());
                    managerList.add(toDoList);
                }
                managerAdapter.notifyDataSetChanged();
                createToDoListDialog.cancel();
            }
        });

        cancelButton.setOnClickListener(v -> {
            editTextNameOfToDoList.setText("");
            createToDoListDialog.cancel();
        });

        createToDoListDialog.show();
    }

    private void handleSaveToDoLists() throws JsonProcessingException {
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
        getMenuInflater().inflate(R.menu.manager_context_menu, menu);

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
        managerAdapter.notifyDataSetChanged();
    }

    private void handleEdit() {
        editTextNameOfToDoList.setText(managerList.get(managerList.indexOf(selectedToDoList)).getName());
        handleCreateToDoList(true);
    }


}
