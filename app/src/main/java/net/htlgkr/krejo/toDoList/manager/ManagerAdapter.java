package net.htlgkr.krejo.toDoList.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ManagerAdapter extends BaseAdapter {
    private ToDoList toDoList;
    private int layoutId;
    private LayoutInflater inflater;
    Context context;

    //todo bau den adapter und moch einfoch des gonze projekt so dass an sinn mocht
    // zb der ToDoAdapter stott ana normalen List<> a TodoList objekt midgem


    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
