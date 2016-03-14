package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TabWidget;

/**
 * Created by Arttu on 23.1.2016.
 */
public class MazeActivity extends Activity {

    private int width;
    private int height;
    GridLayout mazeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maze);
        mazeLayout = (GridLayout) findViewById(R.id.mazeLayout);
        final int[][] maze = new int[][]{
                {0,2,0,0,0,0,0,0,0,0,0},
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
        };
        final int xTilesCount = maze[0].length;
        final int yTilesCount = maze.length;

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
                int imageHeight = height / yTilesCount;
                int imageWidth = width / xTilesCount;
                Log.e("Cursor Object", mazeLayout.getMeasuredHeight() + "*5 height");
                Log.e("Cursor Object", mazeLayout.getMeasuredHeight() + "*5 height");
                Bitmap imageBitmapWall = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("wall", "drawable", getPackageName()));
                Bitmap wall = Bitmap.createScaledBitmap(imageBitmapWall, imageWidth, imageHeight, false);
                Bitmap imageBitmapFloor = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("floor", "drawable", getPackageName()));
                Bitmap floor = Bitmap.createScaledBitmap(imageBitmapFloor, imageWidth, imageHeight, false);
                Bitmap imageBitmapGoal = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("cup", "drawable", getPackageName()));
                Bitmap goal = Bitmap.createScaledBitmap(imageBitmapGoal, imageWidth, imageHeight, false);
                Log.e("Cursor Object", imageHeight + "*5 height");
                Log.e("Cursor Object", imageWidth + "*5 width");
                for (int i = 0, c = 0, r = 0; i < xTilesCount * yTilesCount; i++, c++) {
                    if (c == xTilesCount) {
                        c = 0;
                        r++;
                    }
                    ImageView image = new ImageView(MazeActivity.this);
                    if (maze[r][c] == 0) {
                        image.setImageBitmap(wall);
                    } else if (maze[r][c] == 1) {
                        image.setImageBitmap(floor);
                    } else image.setImageBitmap(goal);

           /*Log.e("Cursor Object", image.getLayoutParams().height + "height");
            Log.e("Cursor Object", image.getLayoutParams().width+"width");*/
                    mazeLayout.addView(image);
                }
            }
        });



    }
    private String TAG = MazeActivity.class.getSimpleName();
    float initialX, initialY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mazeLayout.onTouchEvent(event);

        int action = event.getActionMasked();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();

                Log.d(TAG, "Action was DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "Action was MOVE");
                break;

            case MotionEvent.ACTION_UP:
                float finalX = event.getX();
                float finalY = event.getY();

                Log.d(TAG, "Action was UP");

                if (initialX < finalX) {
                    Log.d(TAG, "Left to Right swipe performed");
                }

                if (initialX > finalX) {
                    Log.d(TAG, "Right to Left swipe performed");
                }

                if (initialY < finalY) {
                    Log.d(TAG, "Up to Down swipe performed");
                }

                if (initialY > finalY) {
                    Log.d(TAG, "Down to Up swipe performed");
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,"Action was CANCEL");
                break;

            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "Movement occurred outside bounds of current screen element");
                break;
        }

        return super.onTouchEvent(event);
    }

}
