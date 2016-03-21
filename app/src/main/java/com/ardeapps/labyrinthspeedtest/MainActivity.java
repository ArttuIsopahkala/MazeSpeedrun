package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MainActivity extends Activity {

    // Create Preference to check if application is going to be called first
    // time.
    SharedPreferences appPref;
    boolean isFirstTime = true;

    //Database
    private SQLiteDatabase db;
    public Cursor cursor;
    private static final String DATABASE_NAME = "ls_database";
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String PLAYER_NAME = "player_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, PLAYER_NAME, TIME};

    ListView listView;
    MazeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);

        // Get preference value to know that is it first time application is
        // being called.
        appPref = getSharedPreferences("isFirstTime", 0);
        isFirstTime = appPref.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            SharedPreferences.Editor editor = appPref.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
        } else {
            // get database instance
            db = (new Database(this)).getWritableDatabase();
            MazeData mazeData = new MazeData();
            ArrayList<MazeData.Maze> mazes = mazeData.getMazeData();
            ArrayList<String> maze_names = new ArrayList<>();
            ArrayList<String> maze_difficulties = new ArrayList<>();
            ArrayList<int[][]> maze_maps = new ArrayList<>();
            ArrayList<String> times = new ArrayList<>();
            cursor = db.query(TABLE_MAZES, resultColumns, null, null, null, null, null, null);
            cursor.moveToFirst();
            int i = 0;
            for(MazeData.Maze maze : mazes){
                maze_names.add(maze.maze_name);
                maze_maps.add(maze.map);
                switch(maze.difficulty){
                    case 1:
                        maze_difficulties.add("Easy");
                        break;
                    case 2:
                        maze_difficulties.add("Medium");
                        break;
                    case 3:
                        maze_difficulties.add("Hard");
                        break;
                }
                if(cursor.getCount()!=0) {
                    if (!cursor.getString(i).equals("")) {
                        times.add(cursor.getString(i));
                    } else times.add("0:00");
                    i++;
                    cursor.moveToNext();
                } else times.add("0:00");
            }

            if(cursor.getCount()!=0){
                if(cursor.moveToFirst()) {

                    do {

                    }while(cursor.moveToNext());
                }
                cursor.close();
            }

            adapter = new MazeAdapter(this, maze_names, maze_difficulties, maze_maps, times);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handles item selections
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                this.startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                this.startActivity(infoIntent);
                return true;
        }
        return false;
    }
}