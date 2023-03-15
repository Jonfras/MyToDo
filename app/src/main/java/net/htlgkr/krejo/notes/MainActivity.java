package net.htlgkr.krejo.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {


    //Todo: Speichern mocha
    //Todo: context menü bei longClick

    private static final String FILE_PATH = "notes.csv";
    private static final String NOTE_SEPARATOR = ":";

    private static List<Note> noteList;

    private NotesAdapter notesAdapter;
    private ListView listView;
    private Button ok;
    private Button cancel;
    private EditText dateTimeEditText;
    private EditText contentEditText;
    private TextView csvEmptyTextView;
    private ImageView calendarIcon;
    private View vDialog;
    private AlertDialog.Builder dialogBuilder = null;
    AlertDialog alertDialog;

    private int mYear;
    private int mMonth;
    private int mDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();


    }

    private void setUp() {

        vDialog = getLayoutInflater().inflate(R.layout.create_note_dialogue_layout, null);
        dialogBuilder = new AlertDialog.Builder(this)
                .setView(vDialog);
        alertDialog = dialogBuilder.create();


        FileInputStream fileInputStream = getInputStream();
        noteList = readCsvIntoList(fileInputStream);

        notesAdapter = new NotesAdapter(noteList, R.layout.list_item_layout, MainActivity.this);

        listView = findViewById(R.id.notesListView);
        listView.setAdapter(notesAdapter);


        ok = vDialog.findViewById(R.id.okButton);
        cancel = vDialog.findViewById(R.id.cancelButton);

        dateTimeEditText = vDialog.findViewById(R.id.dateTimeInputEditText);
        contentEditText = vDialog.findViewById(R.id.contentEditText);



        calendarIcon = vDialog.findViewById(R.id.calendarImageView);

        LocalDate ld = LocalDate.now();
        mYear = ld.getYear();
        mMonth = ld.getMonthValue();
        mDate = ld.getDayOfMonth();


    }

    private List<Note> readCsvIntoList(FileInputStream fileInputStream) {
        List<Note> notes = new ArrayList<>();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(fileInputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (fileScanner.hasNext()) {
            notes.add(Note.deserialize(fileScanner.nextLine()));
        }

        if (notes.isEmpty()) {
            csvEmptyTextView = findViewById(R.id.csvEmptyTextView);
            csvEmptyTextView.setText("No notes found in CSV!");
        } else {
            csvEmptyTextView = findViewById(R.id.csvEmptyTextView);
            ((ViewGroup) csvEmptyTextView.getParent()).removeView(csvEmptyTextView);
        }

        return notes;
    }

    private FileInputStream getInputStream() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(FILE_PATH);
        } catch (FileNotFoundException e) {
            System.err.println("File was not found");
            return null;

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
            case R.id.add ->
                handleMenuBarAdd();

            case R.id.save ->
                handleMenuBarSave();

        }
        return (super.onOptionsItemSelected(item));
    }

    private void handleMenuBarSave() {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_PATH, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fos, true);

        for (Note n :
                noteList) {
            pw.println(Note.serialize(n));
        }
        pw.flush();

        pw.close();
        try {
            fos.close();
        } catch (IOException e) {
            System.err.println("fos couldn't be closed");
        }
    }


    private void handleMenuBarAdd() {
        calendarIcon.setOnClickListener(v -> {

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog,
                    (view, year, month, dayOfMonth) -> dateTimeEditText.setText(year + "-" + month + "-" + dayOfMonth),
                    mYear, mMonth, mDate);

            datePickerDialog.show();
        });


        ok.setOnClickListener(v -> {

            try {
                String[] dateArr = dateTimeEditText.getText().toString().split("-");

                LocalDate localDate = LocalDate.of(Integer.parseInt(dateArr[0]),
                        Integer.parseInt(dateArr[1]),
                        Integer.parseInt(dateArr[2]));

                noteList.add(new Note(localDate, contentEditText.getText().toString()));

                notesAdapter.notifyDataSetChanged();
                if (csvEmptyTextView.getParent() != null) {
                    ((ViewGroup) csvEmptyTextView.getParent()).removeView(csvEmptyTextView);
                }
            } catch (NumberFormatException e) {
                csvEmptyTextView.setText("Not a valid Date!");
            }
        });

        cancel.setOnClickListener(v -> {

            contentEditText.setText("");
            dateTimeEditText.setText("");
            alertDialog.cancel();
        });

        alertDialog.show();
    }
    }
