package net.htlgkr.krejo.toDoList.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.htlgkr.krejo.toDoList.R;

import java.util.List;

public class ManagerAdapter extends BaseAdapter {
    private List<ToDoList> toDoLists;
    private int layoutId;
    private LayoutInflater inflater;
    Context context;

    public ManagerAdapter(List<ToDoList> toDoLists, int layoutId, Context context) {
        this.toDoLists = toDoLists;
        this.layoutId = layoutId;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    public List<ToDoList> getToDoLists() {
        return toDoLists;
    }

    @Override
    public int getCount() {
        return toDoLists.size();
    }

    @Override
    public Object getItem(int position) {
        return toDoLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ToDoList toDoList = toDoLists.get(position);
        View listItem = (convertView == null) ?
                inflater.inflate(this.layoutId, null)
                : convertView;

        TextView textView = listItem.findViewById(R.id.managerListTextView);
        textView.setText(toDoList.getName());

        return listItem;
    }
}
