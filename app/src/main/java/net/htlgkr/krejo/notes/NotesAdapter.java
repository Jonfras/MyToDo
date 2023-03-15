package net.htlgkr.krejo.notes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.List;

public class NotesAdapter extends BaseAdapter {
    public List<Note> getNoteList() {
        return noteList;
    }

    private List<Note> noteList;
    private int layoutId;
    private LayoutInflater inflater;
    Context context;

    public NotesAdapter(List<Note> noteList, int layoutId, Context context) {
        this.noteList = noteList;
        this.layoutId = layoutId;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int i) {
        return noteList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Note note = noteList.get(i);
        View listItem = (view == null) ? inflater.inflate(this.layoutId, null) : view;

        TextView tempTxtView = ((TextView) listItem.findViewById(R.id.dateTimeTextView));
        tempTxtView.setText(note.getLocalDate().toString());
        if (note.getLocalDate().isBefore(LocalDate.now())){
            tempTxtView.setBackgroundColor(Color.RED);
        }
        ((TextView) listItem.findViewById(R.id.noteContentTextView)).setText(note.getNoteContent());

        return listItem;
    }
}
