package com.ardeapps.labyrinthspeedtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite Database class
 */
public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ls_database";
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";

    public Database(Context context) {
        // Context, database name, optional cursor factory, database version
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a new table
        db.execSQL("CREATE TABLE " + TABLE_MAZES + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + MAZE_NAME + " TEXT, " + TIME + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_MAZES);
        onCreate(db);
    }
}
