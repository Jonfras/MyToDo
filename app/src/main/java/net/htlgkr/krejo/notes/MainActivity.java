package net.htlgkr.krejo.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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


    private static final String FILE_PATH = "notes.csv";
    private static final int RQ_PREFERENCES = 1;

    private static List<Note> noteList;

    private NotesAdapter notesAdapter;
    private ListView listView;
    private Button ok;
    private Button cancel;
    private EditText dateTimeEditText;
    private EditText contentEditText;
    private TextView csvEmptyTextView;
    private ImageView calendarIcon;
    private View createNoteView;
    private View detailNoteView;
    private AlertDialog.Builder dialogBuilder = null;


    private int mYear;
    private int mMonth;
    private int mDate;
    private Note selectedNote;


    //Todo: list view einträge xml file mit checkbox mocha damit mas obhakerln ko und de events don handlen
    //Todo: dateiformat von csv auf irgendwos ändern
    //Todo: kontext menü punkt detail löschen und als normales onClick event setzen
    //Todo: vielleicht so an floating button mit neiche todo erstellen mocha

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();

    }

    private void setUp() {

        createNoteView = getLayoutInflater().inflate(R.layout.create_note_dialogue_layout, null);
        detailNoteView = getLayoutInflater().inflate(R.layout.detail_note_dialog_layout, null);


        FileInputStream fileInputStream = getInputStream();
        noteList = readCsvIntoList(fileInputStream);

        notesAdapter = new NotesAdapter(noteList, R.layout.list_item_layout, MainActivity.this);

        listView = findViewById(R.id.notesListView);
        listView.setAdapter(notesAdapter);
        registerForContextMenu(listView);


        ok = createNoteView.findViewById(R.id.okButton);
        cancel = createNoteView.findViewById(R.id.cancelButton);

        dateTimeEditText = createNoteView.findViewById(R.id.dateTimeInputEditText);
        contentEditText = createNoteView.findViewById(R.id.contentEditText);


        calendarIcon = createNoteView.findViewById(R.id.calendarImageView);

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
        } catch (Exception e) {
            e.printStackTrace();
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
            case R.id.add -> handleCreateNote(false);

            case R.id.save -> handleMenuBarSave();

            case R.id.preferences_detail -> handleOnPreferences();

        }
        return (super.onOptionsItemSelected(item));
    }

    private void handleOnPreferences() {
        Intent intent = new Intent(this, MySettingsActivity.class);
        startActivityForResult(intent, RQ_PREFERENCES);
    }

    private void handleMenuBarSave() {
        FileOutputStream fos = null;
        Toast.makeText(MainActivity.this, "Saving...", Toast.LENGTH_SHORT).show();

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

        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
    }


    private void handleCreateNote(boolean edit) {
        try {
            ((ViewGroup) createNoteView.getParent()).removeView(createNoteView);
        } catch (Exception e) {

        }

        dialogBuilder = new AlertDialog.Builder(this)
                .setView(createNoteView);

        AlertDialog createNoteDialog = dialogBuilder.create();


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


                if (edit) {
                    noteList.remove(selectedNote);
                }

                noteList.add(new Note(localDate, contentEditText.getText().toString(), edit));

                noteList.sort(Note::compareTo);


                notesAdapter.notifyDataSetChanged();
                listView.setAdapter(notesAdapter);
                if (csvEmptyTextView.getParent() != null) {
                    ((ViewGroup) csvEmptyTextView.getParent()).removeView(csvEmptyTextView);
                }
                createNoteDialog.cancel();
            } catch (NumberFormatException e) {
                csvEmptyTextView.setText("Not a valid Date!");
            }
        });

        cancel.setOnClickListener(v -> {

            contentEditText.setText("");
            dateTimeEditText.setText("");
            createNoteDialog.cancel();
        });

        createNoteDialog.show();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.my_context_menu, menu);

        selectedNote = noteList.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item -> {
                handleEdit();
            }
            case R.id.delete_item -> {
                handleDelete();
            }
            case R.id.detail_item -> {
                handleDetail();
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    private void handleEdit() {

        dateTimeEditText.setText(selectedNote.getLocalDate().toString());
        contentEditText.setText(selectedNote.getNoteContent());

        handleCreateNote(true);
    }

    private void handleDelete() {
        noteList.remove(selectedNote);
        notesAdapter.notifyDataSetChanged();
    }

    private void handleDetail() {
        try {
            ((ViewGroup) detailNoteView.getParent()).removeView(detailNoteView);
        } catch (Exception e) {

        }

        dialogBuilder = new AlertDialog.Builder(this)
                .setView(detailNoteView);

        AlertDialog detail = dialogBuilder.create();

        TextView tempDateTxtView = (TextView) detailNoteView.findViewById(R.id.detailDateTxtView);
        tempDateTxtView.setText(selectedNote.getLocalDate().toString());
        ((TextView) detailNoteView.findViewById(R.id.detailContentTxtView)).setText(selectedNote.getNoteContent());

        if (selectedNote.getLocalDate().isBefore(LocalDate.now())) {
            tempDateTxtView.setBackgroundColor(Color.RED);
        }

        detail.show();

    }
}
