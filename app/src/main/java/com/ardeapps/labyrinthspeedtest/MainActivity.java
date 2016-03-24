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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    // Create Preference to check if application is going to be called first
    // time.
    SharedPreferences appPref;
    boolean isFirstTime = true;

    //Database
    public SQLiteDatabase db;
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};

    ListView listView;
    MazeAdapter adapter;

    MazeData mazeData = new MazeData();
    ArrayList<MazeData.Maze> mazes = mazeData.getMazeData();
    ArrayList<String> maze_names = new ArrayList<>();
    ArrayList<String> maze_difficulties = new ArrayList<>();
    ArrayList<int[][]> maze_maps = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();

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
        }

        // get database instance
        db = (new Database(this)).getReadableDatabase();

        for(MazeData.Maze maze : mazes){
            maze_names.add(maze.maze_name);
            maze_maps.add(maze.map);
            switch(maze.difficulty){
                case 1:
                    maze_difficulties.add(getString(R.string.easy));
                    break;
                case 2:
                    maze_difficulties.add(getString(R.string.medium));
                    break;
                case 3:
                    maze_difficulties.add(getString(R.string.hard));
                    break;
            }
            cursor = db.query(TABLE_MAZES, resultColumns, MAZE_NAME+"=?", new String[] {maze.maze_name}, null, null, null, null);
            Float bestTime;
            Float nextTime;
            if(cursor.getCount()!=0) {
                cursor.moveToFirst();
                bestTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                do {
                    nextTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                    if(nextTime < bestTime){
                        bestTime = nextTime;
                    }
                } while (cursor.moveToNext());
                times.add(bestTime+"");
            } else times.add(getString(R.string.default_zero));

        }

        adapter = new MazeAdapter(this, maze_names, maze_difficulties, maze_maps, times);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //empty mazes arraylist
        mazes.clear();
        //Toast.makeText(this, "ondestroy ", Toast.LENGTH_LONG).show();
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
    public void showStatistics(View v){
        Intent myIntent = new Intent(this, HighscoreActivity.class);
        int position = listView.getPositionForView(v);
        String name = maze_names.get(position);
        myIntent.putExtra("name", name);
        this.startActivity(myIntent);
    }
}