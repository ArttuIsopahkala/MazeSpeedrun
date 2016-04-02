package com.ardeapps.mazespeedrun;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

        if(times.size()>5) {
            times.subList(5, times.size()).clear();
        }
        adapter = new HighscoreAdapter(getActivity(), times);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_highscore, container, false);
        final int[] CLICKABLES = new int[] {
                R.id.sign_in_button, R.id.sign_out_button
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }

        listview_personal = (ListView) v.findViewById(R.id.listview_personal);
        listview_personal.setAdapter(adapter);

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
