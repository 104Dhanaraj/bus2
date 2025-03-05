package com.example.bus.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.bus.R;
import java.util.List;

public class StopListAdapter extends ArrayAdapter<String> {
    public StopListAdapter(Context context, List<String> stops) {
        super(context, R.layout.item_stop, stops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_stop, parent, false);
        }
        TextView stopNameTextView = convertView.findViewById(R.id.txt_stop_name);
        stopNameTextView.setText(getItem(position));
        return convertView;
    }
}
