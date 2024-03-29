package com.ardeapps.mazespeedrun;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

/**
 * Created by Arttu on 15.3.2016.
 * Adapter for mainFragment map list
 */
public class MazeAdapter extends BaseAdapter{
    public static final String SWITCH_TO_HIGHSCORE = "com.ardeapps.mazespeedrun.SWITCH_TO_HIGHSCORE";
    public static final String SWITCH_TO_MAZE = "com.ardeapps.mazespeedrun.SWITCH_TO_MAZE";

    ArrayList<String> result_name;
    ArrayList<String> result_difficulty;
    ArrayList<int[][]> result_map;
    ArrayList<String> result_time;
    Context context;
    ArrayList<Integer> imageIds;
    ArrayList<Boolean> unLockeds;
    private static LayoutInflater inflater = null;

    public MazeAdapter(Context ctx, ArrayList<String> maze_name, ArrayList<String> maze_difficulty, ArrayList<int[][]> maze_map, ArrayList<String> times, ArrayList<Integer> mapImages, ArrayList<Boolean> unLocked_maps) {
        result_name = maze_name;
        result_difficulty = maze_difficulty;
        result_map = maze_map;
        result_time = times;
        context = ctx;
        imageIds = mapImages;
        unLockeds = unLocked_maps;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result_name.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView title_tv;
        TextView difficulty_tv;
        TextView your_best_tv;
        Button stats_btn;
        ImageView img;
        RelativeLayout lock_overlay;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        holder.lock_overlay = (RelativeLayout) convertView.findViewById(R.id.lock_overlay);

        holder.title_tv = (TextView) convertView.findViewById(R.id.maze_name);
        holder.img = (ImageView) convertView.findViewById(R.id.map_image);
        holder.difficulty_tv = (TextView) convertView.findViewById(R.id.maze_difficulty);
        holder.your_best_tv = (TextView) convertView.findViewById(R.id.your_best);
        holder.stats_btn = (Button) convertView.findViewById(R.id.stats_btn);

        holder.title_tv.setText(result_name.get(position));
        holder.difficulty_tv.setText(result_difficulty.get(position));
        holder.your_best_tv.setText(parent.getResources().getString(R.string.hs_your_best) + ": " + result_time.get(position) + "s");
        holder.img.setImageResource(imageIds.get(position));

        // check if map is unlocked
        if(unLockeds.get(position)){
            holder.lock_overlay.setVisibility(View.INVISIBLE);

            final Bundle mBundle = new Bundle();
            mBundle.putString("name", result_name.get(position));
            mBundle.putSerializable("map", result_map.get(position));
            mBundle.putString("time", result_time.get(position));

            //send broadcast to mainActivity to open highscores or maze
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent hsIntent = new Intent(SWITCH_TO_MAZE);
                    hsIntent.putExtras(mBundle);
                    context.sendBroadcast(hsIntent);
                }
            });
            holder.stats_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent hsIntent = new Intent(SWITCH_TO_HIGHSCORE);
                    hsIntent.putExtras(mBundle);
                    context.sendBroadcast(hsIntent);
                }
            });
        } else {
            holder.lock_overlay.setVisibility(View.VISIBLE);
            holder.stats_btn.setClickable(false);
            convertView.setClickable(false);
        }

        return convertView;
    }
}