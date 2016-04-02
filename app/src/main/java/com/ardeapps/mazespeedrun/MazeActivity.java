package com.ardeapps.mazespeedrun;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Arttu on 23.1.2016.
 */
public class MazeActivity extends Activity {

    //Database
    public SQLiteDatabase db;
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};

    private int width;
    private int height;
    GridLayout mazeLayout;
    private String name = "";
    private int[][] maze;
    float start_x;
    float start_y;
    int imageHeight;
    int imageWidth;
    int xTilesCount;
    int yTilesCount;
    int notification_bar_height;

    boolean gameStarted = false;
    String finalTime = "0.000";

    //timer
    long startTime = 0;
    TextView clockText;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int thousandth = (int) (millis);
            int seconds = thousandth / 1000;
            thousandth = thousandth % 1000;
            clockText.setText(String.format("%01d.%03d", seconds, thousandth));
            timerHandler.postDelayed(this, 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maze);
        mazeLayout = (GridLayout) findViewById(R.id.mazeLayout);
        clockText = (TextView) findViewById(R.id.clockText);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
            maze = (int[][]) extras.getSerializable("map");
        }

        // get database instance
        db = (new Database(this)).getWritableDatabase();

        xTilesCount = maze[0].length;
        yTilesCount = maze.length;

        mazeLayout.setColumnCount(xTilesCount);
        mazeLayout.setRowCount(yTilesCount);
        mazeLayout.removeAllViews();

        mazeLayout.post(new Runnable() {

            @Override
            public void run() {
                width = mazeLayout.getWidth();
                height = mazeLayout.getHeight();
                //do something cool with width and height
                Log.e("Cursor Object", width + "*5 height");
                Log.e("Cursor Object", height + "*5 height");
                imageHeight = height / yTilesCount;
                imageWidth = width / xTilesCount;

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screen_height = size.y;
                notification_bar_height = screen_height-height;
                Bitmap imageBitmapWall = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("wall", "drawable", getPackageName()));
                Bitmap wall = Bitmap.createScaledBitmap(imageBitmapWall, imageWidth, imageHeight, false);
                Bitmap imageBitmapFloor = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("floor", "drawable", getPackageName()));
                Bitmap floor = Bitmap.createScaledBitmap(imageBitmapFloor, imageWidth, imageHeight, false);
                Bitmap imageBitmapGoal = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("cup", "drawable", getPackageName()));
                Bitmap goal = Bitmap.createScaledBitmap(imageBitmapGoal, imageWidth, imageHeight, false);
                for (int i = 0, c = 0, r = 0; i < xTilesCount * yTilesCount; i++, c++) {
                    if (c == xTilesCount) {
                        c = 0;
                        r++;
                    }
                    ImageView image = new ImageView(MazeActivity.this);
                    int imgtype = maze[r][c];
                    switch (imgtype){
                        case 1:
                            //path
                            image.setImageBitmap(floor);
                            break;
                        case 2:
                            //start
                            start_x = c;
                            start_y = r;
                            image.setImageBitmap(wall);
                            break;
                        case 3:
                            //finish
                            image.setImageBitmap(goal);
                            break;
                    }

                    mazeLayout.addView(image);
                }
            }
        });
    }

    float initialX, initialY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mazeLayout.onTouchEvent(event);

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //start timer only if finger touches start tile
                initialX = event.getRawX();
                initialY = event.getRawY();
                if(initialX > (start_x-1)*imageWidth &&
                   initialX < imageWidth*xTilesCount-((xTilesCount-start_x)*imageWidth) &&
                   initialY > notification_bar_height+(start_y)*imageHeight &&
                   initialY < notification_bar_height+(imageHeight*yTilesCount-((yTilesCount-start_y-1)*imageHeight))){
                    Log.d("Cursor Object", "TRUE");
                    //start timer
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    gameStarted = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                switch (whatViewIsIt(event.getRawX(), event.getRawY())){
                    case 0:
                        //wall
                        gameFinishedRight(false);
                        break;
                    case 1:
                        //path
                        Log.d("Cursor Object", "PATH");
                        // TODO: 21.3.2016 maybe some path?
                        break;
                    case 3:
                        //finish, stop timer
                        gameFinishedRight(true);
                        break;
                }
                break;

            case MotionEvent.ACTION_UP:
                gameFinishedRight(false);
                break;
        }

        return super.onTouchEvent(event);
    }
    public void gameFinishedRight(boolean finishedRight){
        CustomResultDialog cdd;
        if(gameStarted) {
            timerHandler.removeCallbacks(timerRunnable);
            if(finishedRight){
                //player reach finish tile
                finalTime = clockText.getText().toString();
                updateHighscores();
                cdd = new CustomResultDialog(MazeActivity.this, name, finalTime);
            } else {
                //player hit wall or release finger
                cdd = new CustomResultDialog(MazeActivity.this, name, getString(R.string.default_zero));
            }
            gameStarted = false;
            clockText.setText(R.string.default_zero);
            cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            cdd.show();
        }
    }

    public int whatViewIsIt(float x, float y) {
        for (int i = 0, c = 0, r = 0; i < xTilesCount * yTilesCount; i++, c++) {
            if (c == xTilesCount) {
                c = 0;
                r++;
            }
            int imgtype = maze[r][c];
            //check what kind of tile finger touches
            if (x > (c - 1) * imageWidth &&
                    x < imageWidth * xTilesCount - ((xTilesCount - c) * imageWidth) &&
                    y > notification_bar_height + r * imageHeight &&
                    y < notification_bar_height + (imageHeight * yTilesCount - ((yTilesCount - r - 1) * imageHeight))) {
                return imgtype;
            }
        }
        return 4;
    }

    public void updateHighscores(){

        //Update local highscores
        cursor = db.query(TABLE_MAZES, resultColumns, MAZE_NAME+"=?", new String[] {name}, null, null, null, null);
        Log.e("Cursor Object", cursor.getCount() + " colmuns");
        if(cursor.getCount() <= 5){
            ContentValues cv = new ContentValues();
            cv.put(MAZE_NAME, name);
            cv.put(TIME, finalTime);
            db.insert(TABLE_MAZES, null, cv);
        } else {

        }

    }
}
