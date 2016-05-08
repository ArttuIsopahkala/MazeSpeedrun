package com.ardeapps.mazespeedrun;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MazeFragment . OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MazeFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MazeFragment extends Fragment {
    private int maze_width;
    private int maze_height;
    GridLayout mazeLayout;
    private String name = "";
    private String time = "-";
    private int[][] maze;
    float start_x;
    float start_y;
    int imageHeight;
    int imageWidth;
    int actionbar_height;
    int xTilesCount;
    int yTilesCount;
    int notification_area_height;
    Bitmap start, start1, floor, finish, finish1;
    ActionBar actionBar;
    ImageView target, startImage, finishImage;
    boolean targetVisible = false;

    boolean newBestTime = false;

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
        void onGameFinished(Float time, String maze_name);
        void onUpdateBestTime(String maze_name);
    }

    Listener mListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString("name");
            maze = (int[][]) getArguments().getSerializable("map");
            time = getArguments().getString("time");
        }
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

                notification_area_height = screen_height - maze_height - actionbar_height;

                imageHeight = (screen_height-notification_area_height) / yTilesCount;
                imageWidth = maze_width / xTilesCount;
                Bitmap imageBitmapStart = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("start", "drawable", getActivity().getPackageName()));
                start = Bitmap.createScaledBitmap(imageBitmapStart, imageWidth, imageHeight, false);
                Bitmap imageBitmapFloor = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("floor", "drawable", getActivity().getPackageName()));
                floor = Bitmap.createScaledBitmap(imageBitmapFloor, imageWidth, imageHeight, false);
                Bitmap imageBitmapFinish = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("finish", "drawable", getActivity().getPackageName()));
                finish = Bitmap.createScaledBitmap(imageBitmapFinish, imageWidth, imageHeight, false);
                Bitmap imageBitmapStart1 = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("start1", "drawable", getActivity().getPackageName()));
                start1 = Bitmap.createScaledBitmap(imageBitmapStart1, imageWidth, imageHeight, false);
                Bitmap imageBitmapFinish1 = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("finish1", "drawable", getActivity().getPackageName()));
                finish1 = Bitmap.createScaledBitmap(imageBitmapFinish1, imageWidth, imageHeight, false);

                startImage = new ImageView(getActivity());
                finishImage = new ImageView(getActivity());
                // Inflate the layout for this fragment
                for (int i = 0, c = 0, r = 0; i < xTilesCount * yTilesCount; i++, c++) {
                    if (c == xTilesCount) {
                        c = 0;
                        r++;
                    }
                    ImageView floorImage = new ImageView(getActivity());
                    Space wall = new Space(getActivity());
                    wall.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
                    int imgtype = maze[r][c];
                    switch (imgtype) {
                        case 0:
                            //wall
                            mazeLayout.addView(wall);
                            break;
                        case 1:
                            //path
                            floorImage.setImageBitmap(floor);
                            mazeLayout.addView(floorImage);
                            break;
                        case 2:
                            //start
                            start_x = c;
                            start_y = r;
                            startImage.setImageBitmap(start);
                            mazeLayout.addView(startImage);
                            break;
                        case 3:
                            //finish
                            finishImage.setImageBitmap(finish);
                            mazeLayout.addView(finishImage);
                            break;
                    }
                }
            }
        });

        /** SET ONTOUCHLISTENER TO HANDLE TIME AND GAME LOGIC*/
        mazeLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                float initialX, initialY;
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        //start timer only if finger touches start tile
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        if(whatViewIsIt(initialX, initialY) == 2){
                            //start timer
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            gameStarted = true;
                            target.setVisibility(View.INVISIBLE);
                            targetVisible = false;
                            startImage.setImageBitmap(start1);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        switch (whatViewIsIt(event.getRawX(), event.getRawY())){
                            case 0:
                                //wall
                                if(!targetVisible && gameStarted){
                                    int x = Math.round(event.getRawX()-(target.getWidth()/2));
                                    int y = Math.round(event.getRawY()-target.getHeight());
                                    target.setX(x);
                                    target.setY(y);
                                    target.setVisibility(View.VISIBLE);
                                    targetVisible = true;
                                }
                                gameFinishedRight(false);
                                break;
                            case 3:
                                //finish, stop timer
                                if(gameStarted){
                                    finishImage.setImageBitmap(finish1);
                                }
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

    public void initializeGame(){
        startImage.setImageBitmap(start);
        finishImage.setImageBitmap(finish);
        clockText.setText(R.string.default_zero);
    }

    public void setMapTime(long requestTime){
        //get time from MazeData for compare and check new highscore
        Float currentTime = (float)requestTime/1000;
        Float previousTime = Float.parseFloat(time);

        newBestTime = false;
        if(currentTime < previousTime || previousTime == 0){
            time = String.format(Locale.ENGLISH,"%.2f", currentTime);
            newBestTime = true;
        }
    }

    public void gameFinishedRight(boolean finishedRight){
        android.support.v4.app.FragmentManager manager = getFragmentManager();
        if(gameStarted) {
            timerHandler.removeCallbacks(timerRunnable);
            if(finishedRight){
                //player reach finish tile
                finalTime = Float.parseFloat(clockText.getText().toString());
                mListener.onGameFinished(finalTime, name);
            } else {
                finalTime = Float.parseFloat(getString(R.string.default_zero));
                clockText.setText(R.string.default_zero);
            }
            gameStarted = false;
            mListener.onUpdateBestTime(name);
            //Show results in dialog
            DialogFragment resultDialog = CustomResultDialog.newInstance(name, finalTime, time, newBestTime);
            resultDialog.setCancelable(false);
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
            if (x > c * imageWidth &&
                    x < imageWidth * xTilesCount - ((xTilesCount - c - 1) * imageWidth) &&
                    y > notification_area_height + r * imageHeight &&
                    y < notification_area_height + (imageHeight * yTilesCount - ((yTilesCount - r - 1) * imageHeight))) {
                return imgtype;
            }
        }
        return 4;
    }
}
