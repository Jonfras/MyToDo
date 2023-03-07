package net.htlgkr.krejo.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    //Todo: 1. ListView mit einträgen erstellen
    //Todo: 2. Dialog zum erstellen machen
    //Todo: 3. ActionMenu anzeigen und verwalten

    private final static String FILE = "notes.csv";

    private NotesAdapter notesAdapter;
    private ListView listView;
    private Button ok;
    private Button cancel;
    private EditText dateTimeEditText;
    private EditText contentEditText;
    private TextView csvEmptyTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();




    }

    private void setUp() {
        FileInputStream fileInputStream;

        if ((fileInputStream = getInputStream()) == null)
        notesAdapter = new NotesAdapter()
    }

    private FileInputStream getInputStream() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(FILE);
        } catch (FileNotFoundException e) {
            System.out.println("File needs to be created...");
            
        }

    }
}