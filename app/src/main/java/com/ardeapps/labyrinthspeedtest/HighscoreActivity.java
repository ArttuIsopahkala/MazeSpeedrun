package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighscoreActivity extends Activity {

    //Database
    public SQLiteDatabase db;
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};
    ListView listview_personal, listview_alltime;
    String name;
    ArrayList<Float> times = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        listview_personal = (ListView) findViewById(R.id.listview_personal);
        listview_alltime = (ListView) findViewById(R.id.listview_alltime);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }

        // get database instance
        db = (new Database(this)).getReadableDatabase();

        cursor = db.query(TABLE_MAZES, resultColumns, MAZE_NAME+"=?", new String[] {name}, null, null, null, null);

        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            do {
                times.add(Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME))));
            } while (cursor.moveToNext());
        }

        Comparator<Float> byFirstElement = new Comparator<Float>() {
            @Override
            public int compare(Float arg0, Float arg1) {
                return Float.compare(arg0, arg1);
            }
        };
        Collections.sort(times, byFirstElement);

        if(times.size()>5) {
            times.subList(5, times.size()).clear();
        }
        ArrayAdapter<Float> adapter = new HighscoreAdapter(this, times);
        listview_personal.setAdapter(adapter);
        listview_alltime.setAdapter(adapter);
    }

}
