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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    //Database
    public SQLiteDatabase db;

    ListView listView;
    MazeAdapter adapter;

    MazeData mazeData = new MazeData();
    ArrayList<MazeData.Maze> mazes;
    ArrayList<String> maze_names = new ArrayList<>();
    ArrayList<String> maze_difficulties = new ArrayList<>();
    ArrayList<int[][]> maze_maps = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    Context context;

    //signing stuff
    String mGreeting;

    public interface Listener {
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
    }

    Listener mListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        mGreeting = getString(R.string.default_greeting);

        // get database instance
        db = (new Database(getActivity())).getReadableDatabase();

    }

    /** Get MazeData for mainFragment items */
    public void updateMazeData(){
        mazes = mazeData.getMazeData();
        // load local times from database and update
        mazeData.loadLocal(db);

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

            if(maze.mapTime > 0){
                Float time = (float)maze.mapTime/1000;
                String timeString = String.format(Locale.ENGLISH,"%.2f", time);
                times.add(timeString);
            } else times.add(getString(R.string.default_zero));
        }

        adapter = new MazeAdapter(context, maze_names, maze_difficulties, maze_maps, times);
    }

    @Override
    public void onPause() {
        super.onPause();
        //empty mazes arraylist
        mazes.clear();
        maze_names.clear();
        maze_difficulties.clear();
        maze_maps.clear();
        times.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateMazeData();

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        AdView mAdView = (AdView) v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
        if(getActivity().getActionBar() != null){
            getActivity().getActionBar().show();
        }
    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView greetingTv = (TextView) getActivity().findViewById(R.id.hello);
        if (greetingTv != null) greetingTv.setText(mGreeting);
    }

}
