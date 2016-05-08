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
 * Class for maze data
 */
public class MazeData {
    //Database
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};

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

        public void unLockNextMap(String map_name){
                for (Maze maze : mazes){
                        if(maze.maze_name.equals(map_name)){
                                int unlock_maze_id = mazes.indexOf(maze)+1;
                                mazes.get(unlock_maze_id).unLocked = true;
                        }
                }
        }
        public int getUnlockedCount(){
            int count = 1;
            for (Maze maze : mazes){
                if(maze.unLocked){
                    count++;
                }
            }
            return count;
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
                            unLockNextMap(maze.maze_name);
                            cursor.moveToFirst();
                            Float currentBestTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                            Float nextTime;
                            do {
                                    nextTime = Float.parseFloat(cursor.getString(cursor.getColumnIndex(TIME)));
                                    if (nextTime < currentBestTime) {
                                            currentBestTime = nextTime;
                                    }
                            } while (cursor.moveToNext());
                            long newTime = (long) (currentBestTime * 1000);
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
            public int imageId;
            public boolean unLocked;

            public Maze(String maze_name, int difficulty, int mapId, int[][] map, long mapTime, int imageId, boolean unLocked) {
                    this.maze_name = maze_name;
                    this.difficulty = difficulty;
                    this.mapId = mapId;
                    this.map = map;
                    this.mapTime = mapTime;
                    this.imageId = imageId;
                    this.unLocked = unLocked;
            }
    }

    public ArrayList<Maze> mazes = new ArrayList<>();

    public ArrayList<MazeData.Maze> getMazeData(){
        mazes.add(new Maze("Map 1", 1, R.string.leaderboard_map_1,new int[][]{
                {0,0,3,0,0},
                {0,0,1,0,0},
                {0,0,1,0,0},
                {1,1,1,0,0},
                {1,0,0,0,0},
                {1,0,1,1,1},
                {1,0,1,0,1},
                {1,1,1,0,1},
                {0,0,0,0,2},
        }, -1, R.drawable.map1, true));
        mazes.add(new Maze("Map 2", 1, R.string.leaderboard_map_2, new int[][]{
                {3,1,1,1,0},
                {0,0,0,1,0},
                {1,1,1,1,0},
                {1,0,0,0,0},
                {1,1,1,1,1},
                {0,0,0,0,1},
                {1,1,1,1,1},
                {1,0,0,0,0},
                {1,1,1,1,2},
        }, -1, R.drawable.map2, false));
        mazes.add(new Maze("Map 3", 1, R.string.leaderboard_map_3, new int[][]{
                {0,1,1,3,0},
                {1,1,0,0,0},
                {1,0,0,0,0},
                {1,1,0,0,0},
                {0,1,1,0,0},
                {0,0,1,1,0},
                {0,0,0,1,1},
                {0,0,0,0,1},
                {2,1,1,1,1},
        }, -1, R.drawable.map3, false));
        mazes.add(new Maze("Map 4", 1, R.string.leaderboard_map_4, new int[][]{
                {2,0,0,0,0},
                {1,0,0,0,0},
                {1,0,0,0,0},
                {1,0,0,0,0},
                {1,1,1,1,0},
                {0,0,0,1,0},
                {0,0,0,1,0},
                {3,1,1,1,0},
                {0,0,0,0,0},
        }, -1, R.drawable.map4, false));
        mazes.add(new Maze("Map 5", 1, R.string.leaderboard_map_5, new int[][]{
                {0,0,0,0,0},
                {1,1,1,1,1},
                {1,0,0,0,1},
                {1,0,3,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {2,0,1,1,1},
        }, -1, R.drawable.map5, false));
        mazes.add(new Maze("Map 6", 1, R.string.leaderboard_map_6, new int[][]{
                {1,1,1,0,0},
                {1,0,1,0,3},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {2,0,1,1,1},
        }, -1, R.drawable.map6, false));
        mazes.add(new Maze("Map 7", 1, R.string.leaderboard_map_7, new int[][]{
                {1,1,1,0,0},
                {1,0,1,0,0},
                {1,0,1,0,0},
                {1,0,1,0,0},
                {1,0,1,0,0},
                {1,0,1,0,0},
                {1,0,1,1,0},
                {1,0,0,1,0},
                {3,0,0,2,0},
        }, -1, R.drawable.map7, false));
        mazes.add(new Maze("Map 8", 1, R.string.leaderboard_map_8, new int[][]{
                {1,1,3,1,0},
                {1,0,0,1,0},
                {1,0,0,1,0},
                {1,1,0,1,0},
                {0,1,0,1,1},
                {0,1,0,0,1},
                {0,1,0,0,1},
                {0,1,1,0,1},
                {0,0,2,1,1},
        }, -1, R.drawable.map8, false));
        mazes.add(new Maze("Map 9", 1, R.string.leaderboard_map_9, new int[][]{
                {0,0,0,0,0},
                {1,1,1,1,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {1,0,1,1,1},
                {1,0,0,0,1},
                {1,0,0,0,1},
                {1,0,0,0,1},
                {3,0,0,0,2},
        }, -1, R.drawable.map9, false));
        mazes.add(new Maze("Map 10", 1, R.string.leaderboard_map_10, new int[][]{
                {0,0,0,3,0},
                {0,0,1,1,0},
                {0,1,1,0,0},
                {1,1,0,0,0},
                {1,0,0,0,0},
                {1,1,0,0,0},
                {0,1,1,0,0},
                {0,0,1,0,0},
                {0,0,1,1,2},
        }, -1, R.drawable.map10, false));
        mazes.add(new Maze("Map 11", 2, R.string.leaderboard_map_11, new int[][]{
                {3,1,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,1,1,1,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,2,0,0},
        }, -1, R.drawable.map11, false));
        mazes.add(new Maze("Map 12", 2, R.string.leaderboard_map_12, new int[][]{
                {0,0,0,0,0,0,0,0},
                {0,1,1,1,1,3,0,0},
                {0,1,0,0,0,0,0,0},
                {0,1,0,0,0,0,0,0},
                {0,1,0,0,0,0,0,0},
                {0,1,0,0,0,0,0,0},
                {0,1,0,0,0,0,0,0},
                {0,1,1,1,1,1,1,0},
                {0,0,0,0,0,0,1,0},
                {0,0,0,0,0,0,1,0},
                {0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,1,1},
                {0,0,0,0,1,1,1,1},
                {0,0,0,1,1,0,1,1},
                {2,1,1,1,1,1,1,1},
        }, -1, R.drawable.map12, false));
        mazes.add(new Maze("Map 13", 2, R.string.leaderboard_map_13, new int[][]{
                {0,0,0,0,0,0,0,0},
                {0,1,1,1,1,1,1,1},
                {0,1,0,0,0,0,0,1},
                {0,1,0,1,1,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,1,0,1},
                {0,1,0,1,0,3,0,1},
                {0,1,0,1,0,0,0,1},
                {0,2,0,1,1,1,1,1},
        }, -1, R.drawable.map13, false));
        mazes.add(new Maze("Map 14", 2, R.string.leaderboard_map_14, new int[][]{
                {1,1,1,1,1,3,0,0},
                {1,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,1,0,0,0,0,0,0},
                {0,1,1,0,0,0,0,0},
                {0,0,1,1,0,0,0,0},
                {0,0,0,1,1,0,0,0},
                {0,0,0,0,1,1,0,0},
                {0,0,0,0,0,1,1,0},
                {0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,2},
        }, -1, R.drawable.map14, false));
        mazes.add(new Maze("Map 15", 2, R.string.leaderboard_map_15, new int[][]{
                {0,0,0,0,0,0,0,0},
                {1,1,1,1,1,0,0,0},
                {1,1,1,1,1,0,0,0},
                {1,1,0,1,1,0,0,0},
                {1,1,0,1,1,0,0,0},
                {1,1,1,1,0,0,0,0},
                {1,1,0,1,0,1,1,1},
                {1,1,0,1,0,1,0,1},
                {1,1,0,1,1,1,0,1},
                {1,1,0,0,0,0,0,1},
                {1,1,0,0,0,0,0,1},
                {1,1,0,0,0,0,0,1},
                {1,1,0,0,0,0,0,1},
                {1,1,0,0,0,0,0,1},
                {2,0,0,0,0,0,0,3},
        }, -1, R.drawable.map15, false));
        mazes.add(new Maze("Map 16", 2, R.string.leaderboard_map_16, new int[][]{
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,3,0,0,0,0,0,0},
                {0,1,0,1,1,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,0,1,0,1,0,0},
                {0,1,1,1,0,2,0,0},
        }, -1, R.drawable.map16, false));
        mazes.add(new Maze("Map 17", 2, R.string.leaderboard_map_17, new int[][]{
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {1,1,1,0,0,1,1,1},
                {1,0,1,0,0,1,0,1},
                {1,0,1,1,1,1,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {2,0,0,0,0,0,0,3},
        }, -1, R.drawable.map17, false));
        mazes.add(new Maze("Map 18", 2, R.string.leaderboard_map_18, new int[][]{
                {0,0,0,0,0,0,0,0},
                {0,0,1,1,1,1,0,0},
                {0,1,1,0,0,1,0,0},
                {1,1,0,0,0,1,0,0},
                {1,0,0,1,1,1,1,1},
                {1,0,0,1,0,1,0,1},
                {1,0,0,1,1,1,0,1},
                {1,0,0,0,0,0,0,1},
                {1,1,1,0,0,0,0,1},
                {0,0,1,1,1,1,0,1},
                {0,0,0,0,0,1,0,1},
                {0,0,0,0,0,1,0,1},
                {0,0,0,0,0,1,0,1},
                {0,3,1,1,1,1,0,1},
                {0,0,0,0,0,0,0,2},
        }, -1, R.drawable.map18, false));
        mazes.add(new Maze("Map 19", 2, R.string.leaderboard_map_19, new int[][]{
                {1,1,1,1,1,1,1,0},
                {1,0,0,0,0,0,1,0},
                {1,1,1,1,1,0,1,0},
                {0,0,0,0,1,0,1,1},
                {1,1,1,1,1,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,1,1,1,0,0,0,1},
                {0,0,0,1,0,0,0,1},
                {1,1,1,1,0,0,0,1},
                {1,0,0,0,0,0,0,1},
                {1,1,1,0,1,1,1,1},
                {0,0,1,0,1,0,0,0},
                {1,1,1,0,1,1,1,1},
                {1,0,0,0,0,0,0,1},
                {2,0,3,1,1,1,1,1},
        }, -1, R.drawable.map19, false));
        mazes.add(new Maze("Map 20", 2, R.string.leaderboard_map_20, new int[][]{
                {0,0,0,1,3,0,0,0},
                {0,0,1,1,0,0,0,0},
                {0,1,1,0,0,0,0,0},
                {1,1,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,0},
                {0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,1},
                {1,1,1,1,1,1,1,1},
                {1,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,0,1},
                {0,0,1,1,1,0,0,1},
                {0,1,1,0,1,1,0,1},
                {2,1,0,0,0,1,1,1},
        }, -1, R.drawable.map20, false));
        mazes.add(new Maze("Map 21", 3, R.string.leaderboard_map_21, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,1,1,1,1,1,1,1,1,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,1,0,0,0,0,0,0,0,1,0},
                {0,3,0,0,0,1,1,1,1,1,0},
                {0,0,0,0,0,1,0,0,0,1,0},
                {0,0,0,0,0,1,0,0,0,1,0},
                {0,0,0,0,0,1,0,0,0,1,0},
                {0,0,1,1,1,1,1,1,1,1,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {1,1,1,1,1,1,0,0,0,0,0},
                {1,0,1,0,0,0,0,0,0,0,0},
                {2,1,1,0,0,0,0,0,0,0,0}
        }, -1, R.drawable.map21, false));

        mazes.add(new Maze("Map 22", 3, R.string.leaderboard_map_22, new int[][]{
                {0,1,1,1,1,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,0,0,0,0},
                {0,1,0,0,1,0,0,3,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,1,0,0,1,0,0,1,0,0,0},
                {0,2,0,0,1,1,1,1,0,0,0}
        }, -1, R.drawable.map22, false));
        mazes.add(new Maze("Map 23", 3, R.string.leaderboard_map_23, new int[][]{
                {0,0,0,0,3,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,1,1,1,1,1,0,0},
                {0,0,0,0,0,0,0,0,1,0,0},
                {0,1,1,1,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,0,0,0,1,0,0},
                {0,1,0,0,1,1,1,1,1,0,0},
                {0,1,0,0,0,0,0,0,0,0,0},
                {0,1,1,1,1,1,1,1,1,0,0},
                {0,0,0,0,0,0,0,0,1,0,0},
                {0,2,1,1,1,1,1,1,1,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0}
        }, -1, R.drawable.map23, false));
        mazes.add(new Maze("Map 24", 3, R.string.leaderboard_map_24, new int[][]{
                {1,1,1,1,1,1,1,1,1,1,1},
                {1,0,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,1,1,1,1,1,0,1},
                {1,0,1,0,0,0,0,0,1,0,1},
                {1,0,1,0,1,1,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,1,0,1,0,1},
                {1,0,1,0,1,0,3,0,1,0,1},
                {1,0,1,0,1,0,0,0,1,0,1},
                {1,0,1,0,1,1,1,1,1,0,1},
                {1,0,1,0,0,0,0,0,0,0,1},
                {2,0,1,1,1,1,1,1,1,1,1}
        }, -1, R.drawable.map24, false));
        mazes.add(new Maze("Map 25", 3, R.string.leaderboard_map_25, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,1,1,3,0,0},
                {0,0,0,0,0,0,1,0,0,0,0},
                {0,0,0,0,1,1,1,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {0,0,1,1,1,0,0,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0},
                {1,1,1,0,0,0,0,0,0,0,0},
                {1,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,0,0,0,0,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0},
                {0,0,1,1,1,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,1,1,1,0,0,0,0},
                {0,0,0,0,0,0,1,0,0,0,0},
                {0,0,0,0,0,0,1,1,1,0,0},
                {0,0,0,0,0,0,0,0,1,0,0},
                {0,0,0,0,0,0,0,0,1,1,1},
                {0,0,0,0,0,0,0,0,0,0,1},
                {0,0,0,0,0,0,0,0,0,0,2}
        }, -1, R.drawable.map25, false));
        mazes.add(new Maze("Map 26", 3, R.string.leaderboard_map_26, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {2,1,1,1,1,0,0,0,0,0,0},
                {1,1,0,0,1,0,1,1,1,1,0},
                {1,1,0,0,1,0,1,0,0,1,0},
                {1,1,0,0,1,0,1,0,0,1,0},
                {1,1,0,0,1,0,1,0,0,1,0},
                {1,1,0,0,1,0,1,0,0,1,0},
                {1,1,0,0,1,1,1,0,0,3,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,0,0,0,0,1,0},
                {1,1,0,0,0,1,1,1,1,1,0},
                {1,1,0,0,0,1,0,0,0,0,0},
                {1,1,0,0,0,1,0,0,0,0,0},
                {1,1,0,0,0,1,0,0,0,0,0},
                {1,1,1,1,1,1,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0}
        }, -1, R.drawable.map26, false));
        mazes.add(new Maze("Map 27", 3, R.string.leaderboard_map_27, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,0,1,1,1,1,1,0},
                {1,0,0,1,0,1,0,0,0,1,0},
                {1,0,0,1,0,1,0,0,0,1,0},
                {1,0,0,1,0,1,0,0,0,1,0},
                {1,0,0,1,1,1,0,1,1,1,0},
                {1,0,0,0,0,0,0,1,0,0,0},
                {1,0,0,0,0,0,0,1,0,0,0},
                {1,1,1,0,0,1,1,1,0,0,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {0,0,1,0,0,1,0,0,0,0,0},
                {0,0,1,0,0,1,1,1,1,0,0},
                {0,0,1,0,0,0,0,0,1,0,0},
                {0,0,1,0,0,0,0,0,1,0,0},
                {0,0,1,1,1,1,1,0,1,1,1},
                {0,0,0,0,0,0,1,0,0,0,1},
                {0,0,0,0,0,0,1,0,0,0,1},
                {0,3,1,1,1,1,1,0,0,0,2}
        }, -1, R.drawable.map27, false));
        mazes.add(new Maze("Map 28", 3, R.string.leaderboard_map_28, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,1,1,1,1,1,1,1,0,0,0},
                {0,1,0,0,0,1,0,1,0,0,0},
                {0,1,0,0,0,1,0,1,0,0,0},
                {0,1,0,0,0,1,0,1,0,0,0},
                {0,1,0,0,0,1,0,1,0,0,0},
                {0,1,0,0,0,1,0,1,0,0,0},
                {0,1,0,0,0,1,0,1,1,1,1},
                {0,3,0,0,0,1,0,0,0,0,1},
                {0,0,0,0,0,1,0,0,0,0,1},
                {1,1,1,1,1,1,0,0,0,0,1},
                {1,0,0,0,0,0,0,0,0,0,1},
                {1,0,0,0,0,1,1,1,1,1,1},
                {1,0,0,0,0,1,0,0,0,0,1},
                {1,0,0,0,0,1,0,0,0,0,1},
                {1,1,1,0,0,1,0,0,0,0,1},
                {0,0,1,0,0,1,0,1,1,1,1},
                {0,0,1,0,0,1,0,1,0,0,0},
                {0,0,1,1,1,1,1,1,0,0,0},
                {0,0,0,0,0,2,0,0,0,0,0}
        }, -1, R.drawable.map28, false));
        mazes.add(new Maze("Map 29", 3, R.string.leaderboard_map_29, new int[][]{
                {0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,0,0,1,1,1,1,0},
                {1,0,0,1,0,0,1,0,0,1,0},
                {1,0,0,1,0,0,1,0,0,1,0},
                {1,0,0,1,0,0,1,0,0,1,0},
                {1,0,0,1,1,3,1,0,0,1,0},
                {1,0,0,0,0,0,0,0,0,1,0},
                {1,0,0,0,0,1,1,1,1,1,0},
                {1,0,0,0,0,1,0,0,0,0,0},
                {1,0,0,0,0,1,1,1,0,0,0},
                {1,1,1,0,0,0,0,1,0,0,0},
                {0,0,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,0,0,0,0,0,0,1},
                {1,0,0,0,0,0,0,0,0,0,1},
                {1,1,1,1,1,1,0,0,1,1,1},
                {0,0,0,0,0,1,0,0,1,0,0},
                {0,0,1,1,1,1,0,0,1,1,1},
                {0,0,1,0,0,0,0,0,0,0,1},
                {0,0,1,0,1,1,1,1,0,0,1},
                {0,0,1,1,1,0,0,1,1,1,2}
        }, -1, R.drawable.map29, false));
        mazes.add(new Maze("Map 30", 3, R.string.leaderboard_map_30, new int[][]{
                {2,1,1,1,0,1,1,1,0,0,0},
                {0,0,0,1,0,1,0,1,0,0,0},
                {0,0,0,1,1,1,0,1,1,1,1},
                {0,0,0,0,0,0,0,0,0,0,1},
                {1,1,1,1,1,1,1,0,0,0,1},
                {1,0,0,0,0,0,1,1,1,1,1},
                {1,1,1,1,1,0,0,0,0,0,0},
                {0,0,0,0,1,0,0,0,0,0,0},
                {1,1,1,1,1,0,0,0,0,0,0},
                {1,0,0,0,0,0,1,1,1,1,1},
                {1,0,0,0,0,0,1,0,0,0,1},
                {1,0,0,0,0,0,1,0,0,1,1},
                {1,1,1,1,1,1,1,0,0,1,0},
                {0,0,0,0,0,0,0,0,0,1,0},
                {1,1,1,1,0,1,1,1,0,1,0},
                {1,0,0,1,0,1,0,1,0,1,0},
                {1,1,0,1,1,1,0,1,1,1,0},
                {0,1,0,0,0,0,0,0,0,0,0},
                {0,1,0,0,0,1,1,1,1,1,0},
                {0,1,1,1,1,1,0,0,0,1,3}
        }, -1, R.drawable.map30, false));
        return mazes;
    }
}
