package com.ardeapps.mazespeedrun;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Arttu on 24.3.2016.
 * Class for create personal highscore list
 */
public class HighscoreAdapter extends ArrayAdapter<Float> {
    ArrayList<Float> times;
    private static LayoutInflater inflater = null;

    public HighscoreAdapter(Context context, ArrayList<Float> data) {
        super(context, R.layout.hs_list_item, data);
        times = data;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return times.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public class ViewHolder {
        TextView position;
        TextView personal_result;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.hs_list_item, parent, false);
        }
        viewHolder = new ViewHolder();
        viewHolder.position = (TextView) convertView.findViewById(R.id.position);
        viewHolder.personal_result = (TextView) convertView.findViewById(R.id.personal_result);
        // Populate the data into the template view using the data object
        viewHolder.position.setText((position+1)+".");
        String timeString = String.format(Locale.ENGLISH,"%.2f", times.get(position));
        viewHolder.personal_result.setText(timeString);
        // Return the completed view to render on screen
        return convertView;
    }

}
