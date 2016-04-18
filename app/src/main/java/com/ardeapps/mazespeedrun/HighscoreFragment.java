package com.ardeapps.mazespeedrun;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HighscoreFragment . OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HighscoreFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class HighscoreFragment extends Fragment implements View.OnClickListener {
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
    ArrayAdapter<Float> adapter;

    //signing stuff
    String mGreeting;

    public interface Listener {
        public void onShowLeaderboardsRequested(String name);
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
    }

    Listener mListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HighscoreFragment", "onCreate()");
        name = getArguments().getString("name");

        // get database instance
        db = (new Database(getActivity())).getReadableDatabase();

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

        adapter = new HighscoreAdapter(getActivity(), times);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_highscore, container, false);
        Button ldButton = (Button) v.findViewById(R.id.show_leaderboards_button);
        ldButton.setOnClickListener(this);

        View emptyView = v.findViewById(R.id.empty_view);

        listview_personal = (ListView) v.findViewById(R.id.listview_personal);
        listview_personal.setEmptyView(emptyView);
        listview_personal.setAdapter(adapter);

        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getActivity().getActionBar() != null){
            getActivity().getActionBar().show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //clear adapter to empty listview
        if(!adapter.isEmpty()){
            adapter.clear();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_leaderboards_button:
                mListener.onShowLeaderboardsRequested(name);
                break;
        }
    }
}
