package net.htlgkr.krejo.toDoList.todo;

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

import net.htlgkr.krejo.notes.R;
import net.htlgkr.krejo.toDoList.settings.MySettingsActivity;

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


public class ToDoListActivity extends AppCompatActivity {
    private static final String FILE_PATH = "notes.csv";
    private static final int RQ_PREFERENCES = 1;

    private static List<ToDo> toDoList;
    private static List<ToDo> uncheckedToDos;

    boolean preferenceSetting;

    private ToDoAdapter toDoAdapter;
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
    private ToDo selectedToDo;


    //Todo: vielleicht so an floating button mit neiche todo erstellen mocha

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_do_list_activity);

        FileInputStream fileInputStream = getInputStream();
        toDoList = readCsvIntoList(fileInputStream);
        syncLists();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesChangeListener = this::preferenceChanged;
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        setUp();
    }

    private void syncLists() {
        uncheckedToDos = toDoList.stream().filter(x -> !x.getChecked()).collect(Collectors.toList());
        uncheckedToDos.sort(ToDo::compareTo);
    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        Map<String, ?> allEntries = sharedPrefs.getAll();
        String sValue = "";

        if (allEntries.get(key) instanceof String) {
            sValue = sharedPrefs.getString(key, "");
        } else if (allEntries.get(key) instanceof Boolean) {
            sValue = String.valueOf(sharedPrefs.getBoolean(key, false));
        }

        syncLists();

        preferenceSetting = (Boolean.parseBoolean(sValue));

        setShownListByPreference();

        listView.setAdapter(toDoAdapter);

        toDoAdapter.notifyDataSetChanged();
    }

    private void setShownListByPreference() {
        toDoList.sort(ToDo::compareTo);
        syncLists();
        uncheckedToDos.sort(ToDo::compareTo);
        toDoAdapter.setNoteList((preferenceSetting) ? toDoList : uncheckedToDos);
    }

    private void setUp() {
        toDoAdapter = new ToDoAdapter(toDoList, R.layout.list_item_layout, ToDoListActivity.this);

        createNoteView = getLayoutInflater().inflate(R.layout.create_note_dialogue_layout, null);
        detailNoteView = getLayoutInflater().inflate(R.layout.detail_note_dialog_layout, null);

        listView = findViewById(R.id.notesListView);
        registerForContextMenu(listView);
        listView.setAdapter(toDoAdapter);

        preferenceChanged(prefs, "showDoneTasksCheckBox");

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
                for (int i = 0; i < toDoAdapter.getNoteList().size(); i++) {
                    ToDo toDo = toDoAdapter.getNoteList().get(i);
                    View view = listView.getChildAt(i);
                    CheckBox cb = view.findViewById(R.id.doneCheckBox);

                    cb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toDo.toggleChecked();
                            System.out.println("note wurde getoggled");
                            syncLists();
                            setShownListByPreference();
                            toDoAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private List<ToDo> readCsvIntoList(FileInputStream fileInputStream) {
        List<ToDo> toDos = new ArrayList<>();
        try {
            Scanner fileScanner = new Scanner(fileInputStream);

            while (fileScanner.hasNext()) {
                ToDo n = ToDo.deserialize(fileScanner.nextLine());

                toDos.add(n);
            }

            if (toDos.isEmpty()) {
                System.out.println("no notes found");
                new AlertDialog.Builder(this)
                        .setMessage("No Notes found in CSV")
                        .setPositiveButton("OK", null)
                        .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        toDos.sort(ToDo::compareTo);

        return toDos;
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
        getMenuInflater().inflate(R.menu.menu_to_do_list, menu);
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
        Toast.makeText(ToDoListActivity.this, "Saving...", Toast.LENGTH_SHORT).show();

        try {
            fos = openFileOutput(FILE_PATH, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fos, true);

        for (ToDo n :
                toDoList) {
            pw.println(ToDo.serialize(n));
        }

        pw.flush();
        pw.close();
        try {
            fos.close();
        } catch (IOException e) {
            System.err.println("fos couldn't be closed");
        }

        Toast.makeText(ToDoListActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(ToDoListActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog, (view, year, month, dayOfMonth)
                    -> dateTimeEditText.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                    mYear,
                    mMonth,
                    mDate);

            datePickerDialog.updateDate(LocalDate.now().getYear(),
                    LocalDate.now().getMonthValue()-1,
                    LocalDate.now().getDayOfMonth());

            datePickerDialog.show();
        });

        ok.setOnClickListener(v -> {
            try {
                String[] dateArr = dateTimeEditText.getText().toString().split("-");

                LocalDate localDate = LocalDate.of(Integer.parseInt(dateArr[0]),
                        Integer.parseInt(dateArr[1]),
                        Integer.parseInt(dateArr[2]));

                if (edit) {
                    toDoList.remove(selectedToDo);
                }

                toDoList.add(new ToDo(localDate, contentEditText.getText().toString(), (edit) ? selectedToDo.getChecked() : false));

                toDoList.sort(ToDo::compareTo);

                syncLists();

                setShownListByPreference();

                toDoAdapter.notifyDataSetChanged();

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

        selectedToDo = toDoAdapter.getNoteList().get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
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
            case R.id.detail_item:
                handleDetail();
                break;
            default:
                return false;
        }
        return false;
    }

    private void handleEdit() {
        dateTimeEditText.setText(selectedToDo.getLocalDate().toString());
        contentEditText.setText(selectedToDo.getNoteContent());

        handleCreateNote(true);
    }

    private void handleDelete() {
        toDoList.remove(selectedToDo);
        toDoAdapter.notifyDataSetChanged();
    }

    private void handleDetail() {
        if (detailNoteView.getParent() != null) {
            ((ViewGroup) detailNoteView.getParent()).removeView(detailNoteView);
        }

        dialogBuilder = new AlertDialog.Builder(this)
                .setView(detailNoteView);

        AlertDialog detail = dialogBuilder.create();

        TextView tempDateTxtView = (TextView) detailNoteView.findViewById(R.id.detailDateTxtView);
        tempDateTxtView.setText(selectedToDo.getLocalDate().toString());
        ((TextView) detailNoteView.findViewById(R.id.detailContentTxtView)).setText(selectedToDo.getNoteContent());

        if (selectedToDo.getLocalDate().isBefore(LocalDate.now())) {
            tempDateTxtView.setBackgroundColor(Color.RED);
        }

        detail.show();
    }

}
