package com.ardeapps.mazespeedrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements View.OnClickListener {
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
    Context context;

    //signing stuff
    String mGreeting;
    SignInButton sign_in_button;
    Button sign_out_button;

    public interface Listener {
        public void onStartGameRequested(boolean hardMode);
        public void onShowLeaderboardsRequested();
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
    }

    Listener mListener = null;
    boolean mShowSignIn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        sign_in_button = (SignInButton) getActivity().findViewById(R.id.sign_in_button);
        sign_out_button = (Button) getActivity().findViewById(R.id.sign_out_button);
        /*sign_in_button.setOnClickListener(this);
        sign_out_button.setOnClickListener(this);*/

        mGreeting = getString(R.string.default_greeting);

        // get database instance
        db = (new Database(getActivity())).getReadableDatabase();

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

        adapter = new MazeAdapter(context, maze_names, maze_difficulties, maze_maps, times);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //empty mazes arraylist
        mazes.clear();
        //Toast.makeText(this, "ondestroy ", Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        final int[] CLICKABLES = new int[] {
                R.id.sign_in_button, R.id.sign_out_button
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView greetingTv = (TextView) getActivity().findViewById(R.id.hello);
        if (greetingTv != null) greetingTv.setText(mGreeting);

        getActivity().findViewById(R.id.sign_in_bar).setVisibility(mShowSignIn ?
                View.VISIBLE : View.GONE);
        getActivity().findViewById(R.id.sign_out_bar).setVisibility(mShowSignIn ?
                View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                mListener.onSignInButtonClicked();
                break;
            case R.id.sign_out_button:
                mListener.onSignOutButtonClicked();
                break;
        }
    }

    public void setShowSignInButton(boolean showSignIn) {
        mShowSignIn = showSignIn;
        updateUi();
    }
}
