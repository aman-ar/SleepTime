package com.example.amanarora.activityrecognitionsimple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aman's Laptop on 5/13/2016.
 */
public class BlockAdapter extends ArrayAdapter<Block> {


    Context context;
    ArrayList<Block> values;


    public BlockAdapter(Context context, ArrayList<Block> values) {
        super(context, 0, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Block block = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // Lookup view for data population
        TextView stateTextView = (TextView) convertView.findViewById(R.id.blockState);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.blockTime);
        // Populate the data into the template view using the data object
        timeTextView.setText(block.getStartTime());
        stateTextView.setText(block.getState());
        // Return the completed view to render on screen
        return convertView;
    }


}


