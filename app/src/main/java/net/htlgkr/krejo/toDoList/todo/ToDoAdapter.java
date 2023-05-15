package net.htlgkr.krejo.toDoList.todo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


import net.htlgkr.krejo.toDoList.R;

import java.time.LocalDate;
import java.util.List;

public class ToDoAdapter extends BaseAdapter {
    public List<ToDo> getNoteList() {
        return toDoList;
    }

    private List<ToDo> toDoList;
    private int layoutId;
    private LayoutInflater inflater;
    Context context;

    public void setNoteList(List<ToDo> toDoList) {
        this.toDoList = toDoList;
    }

    public ToDoAdapter(List<ToDo> toDoList, int layoutId, Context context) {
        this.toDoList = toDoList;
        this.layoutId = layoutId;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return toDoList.size();
    }

    @Override
    public Object getItem(int i) {
        return toDoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ToDo toDo = toDoList.get(i);
        View listItem = (view == null) ?
                inflater.inflate(this.layoutId, null) :
                view;

        TextView tempTxtView =  listItem.findViewById(R.id.dateTimeTextView);
        tempTxtView.setText(toDo.getLocalDate().toString());
        if (toDo.getLocalDate().isBefore(LocalDate.now())){
            tempTxtView.setBackgroundColor(Color.RED);
        } else {
            tempTxtView.setBackgroundColor(Color.WHITE);
        }

        CheckBox checkBox = listItem.findViewById(R.id.doneCheckBox);
        checkBox.setChecked(toDo.getChecked());

        ((TextView) listItem.findViewById(R.id.noteContentTextView)).setText(toDo.getNoteContent());

        return listItem;
    }
}
