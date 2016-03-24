package com.ardeapps.labyrinthspeedtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Arttu on 24.3.2016.
 */
public class HighscoreAdapter extends ArrayAdapter<Float> {
    ArrayList<Float> times;

    public HighscoreAdapter(Context context, ArrayList<Float> data) {
        super(context, R.layout.hs_list_item, data);
        this.times = data;
    }
    // View lookup cache
    private static class ViewHolder {
        TextView position;
        TextView personal_result;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.hs_list_item, parent, false);
            viewHolder.position = (TextView) convertView.findViewById(R.id.position);
            viewHolder.personal_result = (TextView) convertView.findViewById(R.id.personal_result);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.position.setText((position+1)+".");
        viewHolder.personal_result.setText(times.get(position)+"");
        // Return the completed view to render on screen
        return convertView;
    }

}
