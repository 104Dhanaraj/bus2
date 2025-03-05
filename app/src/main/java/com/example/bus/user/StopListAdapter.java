package com.example.bus.user;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bus.R;
import com.example.bus.model.Stop;

import java.util.List;

public class StopListAdapter extends BaseAdapter {
    private Context context;
    private List<Stop> stopList;
    private String selectedStopName = ""; // Current stop being announced

    public StopListAdapter(Context context, List<Stop> stopList) {
        this.context = context;
        this.stopList = stopList;
    }

    @Override
    public int getCount() {
        return stopList.size();
    }

    @Override
    public Object getItem(int position) {
        return stopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_stop, parent, false);
        }

        TextView stopNameText = convertView.findViewById(R.id.txt_stop_name);
        Stop stop = stopList.get(position);

        // Check if this stop is the current stop
        if (stop.getName().equals(selectedStopName)) {
            stopNameText.setTextColor(Color.RED); // Highlight the current stop
            stopNameText.setText("• " + stop.getName()); // Add a bullet point
        } else {
            stopNameText.setTextColor(Color.BLACK);
            stopNameText.setText(stop.getName()); // Reset text
        }

        return convertView;
    }

    // Method to update the selected stop
    public void updateSelectedStop(String stopName) {
        this.selectedStopName = stopName;
        notifyDataSetChanged(); // Refresh the list
    }
}
