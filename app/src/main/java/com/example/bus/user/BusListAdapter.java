package com.example.bus.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.bus.R;
import com.example.bus.model.Bus;
import java.util.List;

public class BusListAdapter extends BaseAdapter {
    private Context context;
    private List<Bus> busList;

    public BusListAdapter(Context context, List<Bus> busList) {
        this.context = context;
        this.busList = busList;
    }

    @Override
    public int getCount() {
        return busList.size();
    }

    @Override
    public Object getItem(int position) {
        return busList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bus, parent, false);
        }

        TextView busName = convertView.findViewById(R.id.bus_name);
        TextView busDetails = convertView.findViewById(R.id.bus_details);

        Bus bus = busList.get(position);

        // Add "Suggested" to the first bus
        if (position == 0) {
            busName.setText("Suggested: " + bus.getBusName());
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));
            busName.setTextSize(18);
            busName.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            busName.setText(bus.getBusName());
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            busName.setTextSize(16);
            busName.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        busDetails.setText("Fare: ₹" + bus.getFare() + " | Time: " + bus.getTotalTime() + " mins");

        return convertView;
    }
}
