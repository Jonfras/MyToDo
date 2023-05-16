package net.htlgkr.krejo.toDoList.management.ToDoList;

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
import com.fasterxml.jackson.databind.ObjectMapper;

import net.htlgkr.krejo.toDoList.R;
import net.htlgkr.krejo.toDoList.management.ToDoList.data.ToDoList;
import net.htlgkr.krejo.toDoList.management.settings.SettingsActivity;
import net.htlgkr.krejo.toDoList.management.toDo.ToDo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;


public class ToDoListActivity extends AppCompatActivity {
    ToDoList toDoList;
    private static final String FILE_PATH = "recentToDoList.json";
    private static final int RQ_PREFERENCES = 1;
    boolean preferenceSetting;

    private ToDoListAdapter toDoListAdapter;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_to_do_list);
        Bundle bundle = getIntent().getExtras();
        toDoList = (ToDoList) bundle.get("toDoList");


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

        toDoList.syncLists();

        preferenceSetting = (Boolean.parseBoolean(sValue));

        setShownListByPreference();

        listView.setAdapter(toDoListAdapter);

        toDoListAdapter.notifyDataSetChanged();
    }

    private void setShownListByPreference() {
        toDoList.sortLists(ToDo::compareTo);
        toDoList.syncLists();
        toDoList.sortLists(ToDo::compareTo);
        toDoListAdapter.setNoteList((preferenceSetting) ? toDoList.getToDoList() : toDoList.getToDoListWithoutDoneTasks());
    }

    private void setUp() {
        toDoListAdapter = new ToDoListAdapter(toDoList.getToDoList(), R.layout.list_item_to_do_list, ToDoListActivity.this);

        createNoteView = getLayoutInflater().inflate(R.layout.dialog_create_to_do, null);
        detailNoteView = getLayoutInflater().inflate(R.layout.dialog_detail_note, null);

        listView = findViewById(R.id.notesListView);
        registerForContextMenu(listView);
        listView.setAdapter(toDoListAdapter);

        preferenceChanged(prefs, "showDoneTasksCheckBox");

        ok = createNoteView.findViewById(R.id.addToDoOkButton);
        cancel = createNoteView.findViewById(R.id.addToDoCancelButton);

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
                for (int i = 0; i < toDoListAdapter.getNoteList().size(); i++) {
                    ToDo toDo = toDoListAdapter.getNoteList().get(i);
                    View view = listView.getChildAt(i);
                    CheckBox cb = view.findViewById(R.id.doneCheckBox);

                    cb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toDo.toggleChecked();
                            System.out.println("note wurde getoggled");
                            toDoList.syncLists();
                            setShownListByPreference();
                            toDoListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_to_do_list, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addToDo: {
                handleCreateToDo(false);
                break;
            }
            case R.id.preferences_detail: {
                handleOnPreferences();
                break;
            }

            default: {
                }
        }
        return (super.onOptionsItemSelected(item));
    }

    private void handleOnPreferences() {
        Intent intent = new Intent(this, SettingsActivity.class);
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

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(toDoList);
        pw.println(jsonString);

        pw.flush();
        pw.close();
        try {
            fos.close();
        } catch (IOException e) {
            System.err.println("fos couldn't be closed");
        }

        Toast.makeText(ToDoListActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
    }


    private void handleCreateToDo(boolean edit) {
        if (createNoteView.getParent() != null) {
            ((ViewGroup) createNoteView.getParent()).removeView(createNoteView);
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
                    LocalDate.now().getMonthValue() - 1,
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
                    toDoList.getToDoList().remove(selectedToDo);
                }

                toDoList.getToDoList().add(new ToDo(localDate, contentEditText.getText().toString(), (edit) ? selectedToDo.getChecked() : false));

                toDoList.sortLists(ToDo::compareTo);

                toDoList.sortLists(ToDo::compareTo);

                setShownListByPreference();

                toDoListAdapter.notifyDataSetChanged();

                createNoteDialog.cancel();

                handleMenuBarSave();

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
        getMenuInflater().inflate(R.menu.context_menu_to_do_list, menu);

        selectedToDo = toDoListAdapter.getNoteList().get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
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

        handleCreateToDo(true);
    }

    private void handleDelete() {
        toDoList.getToDoList().remove(selectedToDo);
        toDoListAdapter.notifyDataSetChanged();
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
