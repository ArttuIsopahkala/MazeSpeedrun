package com.ardeapps.mazespeedrun;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MazeFragment . OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MazeFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MazeFragment extends Fragment {
    //Database
    public SQLiteDatabase db;
    public Cursor cursor;
    public final String TABLE_MAZES = "mazes";
    public final String MAZE_NAME = "maze_name";
    public final String TIME = "time";
    String[] resultColumns = new String[]{"_id", MAZE_NAME, TIME};
    ArrayList<Float> times = new ArrayList<>();

    private int maze_width;
    private int maze_height;
    GridLayout mazeLayout;
    private String name = "";
    private int[][] maze;
    float start_x;
    float start_y;
    int imageHeight;
    int imageWidth;
    int actionbar_height;
    int xTilesCount;
    int yTilesCount;
    int notification_area_height;
    Bitmap wall, floor, goal;
    ActionBar actionBar;
    ImageView target;
    boolean targetVisible = false;

    boolean gameStarted = false;
    Float finalTime = 0.0f;

    //timer
    long startTime = 0;
    TextView clockText;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int hundreths = (int) (millis/10);
            int seconds = hundreths / 100;
            hundreths = hundreths % 100;
            clockText.setText(String.format("%01d.%02d", seconds, hundreths));
            timerHandler.postDelayed(this, 10);
        }
    };

    public interface Listener {
        public void onGameFinished(Float time, String maze_name);
    }

    Listener mListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString("name");
            maze = (int[][]) getArguments().getSerializable("map");
        }

        // get database instance
        db = (new Database(getActivity())).getWritableDatabase();
    }
    @Override
    public void onResume() {
        super.onResume();
        //hide actionbar
        if(getActivity().getActionBar() != null){
            actionBar = getActivity().getActionBar();
            actionbar_height = actionBar.getHeight();
            actionBar.hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** GENERATE MAZE FROM MazeData class */
        View v = inflater.inflate(R.layout.fragment_maze, container, false);
        mazeLayout = (GridLayout) v.findViewById(R.id.mazeLayout);
        clockText = (TextView) v.findViewById(R.id.clockText);
        target = (ImageView) v.findViewById(R.id.target);
        target.setVisibility(View.INVISIBLE);

        xTilesCount = maze[0].length;
        yTilesCount = maze.length;

        mazeLayout.setColumnCount(xTilesCount);
        mazeLayout.setRowCount(yTilesCount);
        mazeLayout.removeAllViews();

        mazeLayout.post(new Runnable() {

            @Override
            public void run() {
                maze_width = mazeLayout.getWidth();
                maze_height = mazeLayout.getHeight();

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screen_height = size.y;
                int screen_width = size.x;

                notification_area_height = screen_height - maze_height - actionbar_height;

                imageHeight = (screen_height-notification_area_height) / yTilesCount;
                imageWidth = maze_width / xTilesCount;

                Bitmap imageBitmapWall = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("wall", "drawable", getActivity().getPackageName()));
                wall = Bitmap.createScaledBitmap(imageBitmapWall, imageWidth, imageHeight, false);
                Bitmap imageBitmapFloor = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("floor", "drawable", getActivity().getPackageName()));
                floor = Bitmap.createScaledBitmap(imageBitmapFloor, imageWidth, imageHeight, false);
                Bitmap imageBitmapGoal = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("cup", "drawable", getActivity().getPackageName()));
                goal = Bitmap.createScaledBitmap(imageBitmapGoal, imageWidth, imageHeight, false);

                // Inflate the layout for this fragment
                for (int i = 0, c = 0, r = 0; i < xTilesCount * yTilesCount; i++, c++) {
                    if (c == xTilesCount) {
                        c = 0;
                        r++;
                    }
                    ImageView image = new ImageView(getActivity());
                    int imgtype = maze[r][c];
                    switch (imgtype) {
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

        /** SET ONTOUCHLISTENER TO HANDLE TIME AND GAMELOGIC*/

        mazeLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                float initialX, initialY;
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        //start timer only if finger touches start tile
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        if(initialX > (start_x-1)*imageWidth &&
                                initialX < imageWidth*xTilesCount-((xTilesCount-start_x)*imageWidth) &&
                                initialY > notification_area_height+(start_y)*imageHeight &&
                                initialY < notification_area_height+(imageHeight*yTilesCount-((yTilesCount-start_y-1)*imageHeight))){
                            //start timer
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            gameStarted = true;
                            target.setVisibility(View.INVISIBLE);
                            targetVisible = false;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        switch (whatViewIsIt(event.getRawX(), event.getRawY())){
                            case 0:
                                //wall
                                gameFinishedRight(false);
                                if(!targetVisible){
                                    int x = Math.round(event.getRawX()-(target.getWidth()/2));
                                    int y = Math.round(event.getRawY()-target.getHeight());
                                    target.setX(x);
                                    target.setY(y);
                                    target.setVisibility(View.VISIBLE);
                                    targetVisible = true;
                                }
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

                    case MotionEvent.ACTION_POINTER_DOWN:
                        gameFinishedRight(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        gameFinishedRight(false);
                        break;
                }
                return true;
            }
        });

        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void gameFinishedRight(boolean finishedRight){
        android.support.v4.app.FragmentManager manager = getFragmentManager();

        if(gameStarted) {
            timerHandler.removeCallbacks(timerRunnable);
            if(finishedRight){
                //player reach finish tile
                finalTime = Float.parseFloat(clockText.getText().toString());
                mListener.onGameFinished(finalTime, name);
            } else finalTime = Float.parseFloat(getString(R.string.default_zero));
            gameStarted = false;
            clockText.setText(R.string.default_zero);

            DialogFragment resultDialog = CustomResultDialog.newInstance(name, finalTime);
            resultDialog.show(manager, "resultDialog");
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
                    y > notification_area_height + r * imageHeight &&
                    y < notification_area_height + (imageHeight * yTilesCount - ((yTilesCount - r - 1) * imageHeight))) {
                return imgtype;
            }
        }
        return 4;
    }
}
