package com.ardeapps.labyrinthspeedtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Arttu on 15.3.2016.
 */
public class MazeAdapter extends BaseAdapter {
    ArrayList<String> result_name;
    ArrayList<String> result_difficulty;
    ArrayList<int[][]> result_map;
    ArrayList<String> result_time;
    Context context;
    int[] imageId;
    private static LayoutInflater inflater = null;

    public MazeAdapter(MainActivity mainActivity, ArrayList<String> maze_name, ArrayList<String> maze_difficulty, ArrayList<int[][]> maze_map, ArrayList<String> times) {
        // TODO Auto-generated constructor stub
        result_name = maze_name;
        result_difficulty = maze_difficulty;
        result_map = maze_map;
        result_time = times;
        context = mainActivity;
        //imageId=prgmImages;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result_name.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView title_tv;
        TextView difficulty_tv;
        TextView your_best_tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        holder.title_tv = (TextView) convertView.findViewById(R.id.maze_name);
        // holder.img=(ImageView) rowView.findViewById(R.id.maze_difficulty);
        holder.difficulty_tv = (TextView) convertView.findViewById(R.id.maze_difficulty);
        holder.your_best_tv = (TextView) convertView.findViewById(R.id.your_best);

        holder.title_tv.setText(result_name.get(position)+" - "+result_difficulty.get(position));
        holder.difficulty_tv.setText(result_difficulty.get(position));
        holder.your_best_tv.setText(result_time.get(position));
        //holder.img.setImageResource(imageId[position]);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mazeIntent = new Intent(context, MazeActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("name", result_name.get(position));
                mBundle.putSerializable("map", result_map.get(position));
                mazeIntent.putExtras(mBundle);
                context.startActivity(mazeIntent);
            }
        });
        return convertView;
    }
}