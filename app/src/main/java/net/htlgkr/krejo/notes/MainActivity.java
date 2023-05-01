package net.htlgkr.krejo.notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {


    private static final String FILE_PATH = "notes.csv";
    private static final int RQ_PREFERENCES = 1;

    private static List<Note> noteList;
    private static List<Note> uncheckedNotes;

    private NotesAdapter notesAdapter;
    private ListView listView;
    private Button ok;
    private Button cancel;
    private EditText dateTimeEditText;
    private EditText contentEditText;

    private ImageView calendarIcon;
    private View createNoteView;
    private View detailNoteView;
    private AlertDialog.Builder dialogBuilder = null;

    private SharedPreferences prefs;

    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;


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

        FileInputStream fileInputStream = getInputStream();
        noteList = readCsvIntoList(fileInputStream);
        uncheckedNotes = noteList.stream().filter(x -> !x.getChecked()).collect(Collectors.toList());

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener);


        setUp();

    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        Map<String, ?> allEntries = sharedPrefs.getAll();
        String sValue = "";

        if (allEntries.get(key) instanceof String) {
            sValue = sharedPrefs.getString(key, "");

        } else if (allEntries.get(key) instanceof Boolean) {
            sValue = String.valueOf(sharedPrefs.getBoolean(key, false));
        }

        List<Note> list = (Boolean.parseBoolean(sValue)) ? noteList : uncheckedNotes;

        notesAdapter = new NotesAdapter(list, R.layout.list_item_layout, MainActivity.this);

        listView.setAdapter(notesAdapter);

        notesAdapter.notifyDataSetChanged();
    }


    private void setUp() {

        createNoteView = getLayoutInflater().inflate(R.layout.create_note_dialogue_layout, null);
        detailNoteView = getLayoutInflater().inflate(R.layout.detail_note_dialog_layout, null);

        preferenceChanged(prefs, "showDoneTasksCheckBox");

        listView = findViewById(R.id.notesListView);
        registerForContextMenu(listView);
        listView.setAdapter(notesAdapter);


        ok = createNoteView.findViewById(R.id.okButton);
        cancel = createNoteView.findViewById(R.id.cancelButton);

        dateTimeEditText = createNoteView.findViewById(R.id.dateTimeInputEditText);
        contentEditText = createNoteView.findViewById(R.id.contentEditText);


        calendarIcon = createNoteView.findViewById(R.id.calendarImageView);

        LocalDate ld = LocalDate.now();
        mYear = ld.getYear();
        mMonth = ld.getMonthValue();
        mDate = ld.getDayOfMonth();

        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                for (int i = 0; i < notesAdapter.getNoteList().size(); i++) {
                    Note note = notesAdapter.getNoteList().get(i);
                    View view = listView.getChildAt(i);
                    CheckBox cb = view.findViewById(R.id.doneCheckBox);

                    cb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            note.toggleChecked();
                            System.out.println("note wurde getoggled");
                        }
                    });
                }
            }
        });
    }



    private List<Note> readCsvIntoList(FileInputStream fileInputStream) {
        List<Note> notes = new ArrayList<>();
        try {
            Scanner fileScanner = new Scanner(fileInputStream);

            while (fileScanner.hasNext()) {
                Note n = Note.deserialize(fileScanner.nextLine());

                notes.add(n);
            }

            if (notes.isEmpty()) {
                System.out.println("no notes found");
                new AlertDialog.Builder(this)
                        .setMessage("No Notes found in CSV")
                        .setPositiveButton("OK", null)
                        .show();
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
            case R.id.add:
                handleCreateNote(false);
                break;

            case R.id.save:
                try {
                    handleMenuBarSave();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;

            case R.id.preferences_detail:
                handleOnPreferences();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return (super.onOptionsItemSelected(item));
    }

    private void handleOnPreferences() {
        Intent intent = new Intent(this, MySettingsActivity.class);
        startActivityForResult(intent, RQ_PREFERENCES);
    }

    private void handleMenuBarSave() throws JsonProcessingException {
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


                createNoteDialog.cancel();
            } catch (Exception e) {
                System.out.println("Not a valid Date");
                new AlertDialog.Builder(this)
                        .setMessage("Not a valid Date!")
                        .setPositiveButton("ok", null)
                        .show();
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
            case R.id.edit_item:
                handleEdit();
                break;

            case R.id.delete_item:
                handleDelete();
                break;

            case R.id.detail_item:
                handleDetail();
                break;

            default:
                return false;

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
