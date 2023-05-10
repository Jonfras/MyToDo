package net.htlgkr.krejo.toDoList.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.todo.ToDoListActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerActivity extends AppCompatActivity {
    //todo: speichern auf sd - karte
    //todo: mehrere Todo-Listen mocha
    //-> todo: neiches xml file
    //-> todo:

    private static List<ToDoList> managerList = new ArrayList<>();
    private static final String FILENAME = "notes.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        try {
            managerList = readJSON(getInputStream(FILENAME));
        } catch (FileNotFoundException e) {
            Toast.makeText(ManagerActivity.this, "No ToDoListsFound in " + FILENAME, Toast.LENGTH_SHORT).show();
        }



    }

    public void startNewActivity(View view) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        startActivity(intent);
    }

    private List<ToDoList> readJSON(FileInputStream fileInputStream) {
        List<ToDoList> managerList = new ArrayList<>();

        Scanner fileScanner = new Scanner(fileInputStream);
        String json;

        while (fileScanner.hasNext()) {
            json = fileScanner.nextLine();

            if (json.isEmpty()) {
                System.out.println("no todoLists found");
                new AlertDialog.Builder(this)
                        .setMessage("No ToDoLists found in file")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                managerList.add(new ObjectMapper().convertValue(json, ToDoList.class));
            }
        }


        return managerList;
    }

    private FileInputStream getInputStream(String filename) throws FileNotFoundException{
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(filename);
        } catch (FileNotFoundException e) {
            System.err.println("File was not found");
            throw new FileNotFoundException();
        }
        return fileInputStream;
    }
}
