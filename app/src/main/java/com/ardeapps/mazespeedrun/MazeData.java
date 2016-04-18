package com.ardeapps.mazespeedrun;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Arttu on 14.3.2016.
 */
public class MazeData {
    //Database
    public SQLiteDatabase db;
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};

    public ArrayList<Maze> mazes = new ArrayList<>();

    public ArrayList<MazeData.Maze> getMazeData(){
        mazes.add(new Maze("Map 1", 1, R.string.leaderboard_map_1,new int[][]{
                {0,3,0,0,0},
                {0,1,1,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,1},
                {0,0,0,0,2},
        }, -1));
        mazes.add(new Maze("Map 2", 2, R.string.leaderboard_map_2, new int[][]{
                {0,3,0,0,0},
                {0,1,1,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {0,0,0,1,1},
                {0,0,0,2,0},
        }, -1));
        mazes.add(new Maze("Map 3", 3, R.string.leaderboard_map_3, new int[][]{
                {0,3,0,0,0,0,0,0,0,0,0},
                {0,1,1,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,1,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,1,1,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,2,0,0,0,0,0,0,0}
        }, -1));
        mazes.add(new Maze("Map 4", 1, R.string.leaderboard_map_4, new int[][]{
                {0,3,0,0,0,0,0,0},
                {0,1,1,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,1,0,0,0},
                {0,0,0,0,1,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,1,1,1,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,1,0,0,0,0},
                {0,0,0,2,0,0,0,0},
        }, -1));
        mazes.add(new Maze("Map 5", 1, R.string.leaderboard_map_5, new int[][]{
                {0,3,0,0,0,0,0,0,0,0,0},
                {0,1,1,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0,0,0,0,0},
                {0,0,0,2,0,0,0,0,0,0,0}
        }, -1));
        return mazes;
    }

    public int getMapId(String map_name){
        for (Maze maze : mazes){
            if(maze.maze_name.equals(map_name)){
                return maze.mapId;
            }
        }
        return 0;
    }

    public long getMapTime(String map_name){
        for (Maze maze : mazes){
            if(maze.maze_name.equals(map_name)){
                return maze.mapTime;
            }
        }
        return 0;
    }

    public void setNewMapTime(String map_name, long timeToCloud){
        for (Maze maze : mazes){
            if(maze.maze_name.equals(map_name)){
                maze.mapTime = timeToCloud;
            }
        }
    }

    public boolean isEmpty(){
        for (Maze maze : mazes) {
            if(maze.mapTime > 0){
                return false;
            }
        }
        return true;
    }

    /** SAVE NEW TIME TO SQLITE DATABASE */
    public void saveLocal(SQLiteDatabase db, Float finalTime, String maze_name) {
        Float currentWorstTime = finalTime; //for ex. 1.235
        String worstId ="0";
        Float nextTime;
        //Update local highscores
        cursor = db.query(TABLE_MAZES, resultColumns, MAZE_NAME+"=?", new String[] {maze_name}, null, null, null, null);

        ContentValues cv = new ContentValues();
        cv.put(MAZE_NAME, maze_name);
        cv.put(TIME, String.format(Locale.ENGLISH, "%.2f", finalTime));

        //just insert when under 5 results
        if(cursor.getCount() < 5) {
            db.insert(TABLE_MAZES, null, cv);
        } else {
            //5 results, find worst one and update it
            cursor.moveToFirst();
            do {
                nextTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                if(currentWorstTime < nextTime){
                    currentWorstTime = nextTime;
                    worstId = cursor.getString(cursor.getColumnIndex("_id"));
                }
            } while (cursor.moveToNext());
            if(!currentWorstTime.equals(finalTime)){
                db.update(TABLE_MAZES, cv, "_id=?", new String[]{worstId});
            }
        }
    }
    /** LOAD BEST TIMES FROM DATABASE FOR COMPARATION */
    public void loadLocal(SQLiteDatabase db) {
        //Update local highscores
        for (Maze maze : mazes) {
            cursor = db.query(TABLE_MAZES, resultColumns, MAZE_NAME + "=?", new String[]{maze.maze_name}, null, null, null, null);
            if(cursor.moveToFirst()) {
                cursor.moveToFirst();
                Float currentBestTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                Float nextTime;
                do {
                    Log.e("times", Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME))) + " time");
                    nextTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                    if (nextTime < currentBestTime) {
                        currentBestTime = nextTime;
                    }
                } while (cursor.moveToNext());
                long newTime = (long) (currentBestTime * 1000);
                Log.e("times", "name:" + maze.maze_name + " newTime:" + newTime);
                setNewMapTime(maze.maze_name, newTime);
            }
        }
    }

    //1=easy, 2=medium, 3=difficult
    class Maze {
        public String maze_name;
        public int difficulty;
        public int mapId;
        public int[][] map;
        public long mapTime;

        public Maze(String maze_name, int difficulty, int mapId, int[][] map, long mapTime) {
            this.maze_name = maze_name;
            this.difficulty = difficulty;
            this.mapId = mapId;
            this.map = map;
            this.mapTime = mapTime;
        }
    }
}
