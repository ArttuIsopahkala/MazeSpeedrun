package com.ardeapps.mazespeedrun;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


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
    ListView listview_personal;
    String name;
    ArrayList<Float> times = new ArrayList<>();
    ArrayAdapter<Float> adapter;
    //precise stats
    String time_today = "-";
    String pos_today = "-";
    String time_thisweek = "-";
    String pos_thisweek = "-";
    String time_alltime = "-";
    String pos_alltime = "-";
    String global_top_today = "-";
    String global_top_thisweek = "-";
    String global_top_alltime = "-";

    boolean loggedIn = false;

    public interface Listener {
        public void onShowLeaderboardsRequested(String name);
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
        public void onGetGlobalHighscoresRequest(String name);
    }

    Listener mListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HighscoreFragment", "onCreate()");
        name = getArguments().getString("name");
    }

    public void loadPersonalHighscores(){
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

        loadPersonalHighscores();
        mListener.onGetGlobalHighscoresRequest(name);

        TextView hs_map_title = (TextView) v.findViewById(R.id.map_title);
        hs_map_title.setText(name);

        Button ldButton = (Button) v.findViewById(R.id.show_leaderboards_button);
        ImageView refreshButton = (ImageView) v.findViewById(R.id.refresh_btn);
        ldButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

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
        updateUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //clear adapter to empty listview
        if(!adapter.isEmpty()){
            adapter.clear();
        }
        time_today = "-";
        pos_today = "-";
        time_thisweek = "-";
        pos_thisweek = "-";
        time_alltime = "-";
        pos_alltime = "-";
        global_top_today = "-";
        global_top_thisweek = "-";
        global_top_alltime = "-";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_leaderboards_button:
                mListener.onShowLeaderboardsRequested(name);
                break;
            case R.id.refresh_btn:
                mListener.onGetGlobalHighscoresRequest(name);
        }
    }

    public void showGlobalScores(boolean mLoggedIn){
        loggedIn = mLoggedIn;
        updateUi();
    }

    public String getName(){
        return name;
    }

    public String formatTimeToString(long time){
        Float fTime = (float)time/1000;
        if(fTime == 0){
            return "-";
        }
        return String.format(Locale.ENGLISH,"%.2f", fTime);
    }

    public String formatPosToString(long pos){
        if(pos == 0){
            return "-";
        }
        return pos+".";
    }

    void updateUi() {
        if (getActivity() == null) return;
        if(this.isVisible()) {
            TextView tv_your_best_today = (TextView) getActivity().findViewById(R.id.your_best_today);
            tv_your_best_today.setText(time_today);
            TextView tv_position_today = (TextView) getActivity().findViewById(R.id.position_today);
            tv_position_today.setText(pos_today);

            TextView tv_your_best_thisweek = (TextView) getActivity().findViewById(R.id.your_best_thisweek);
            tv_your_best_thisweek.setText(time_thisweek);
            TextView tv_position_thisweek = (TextView) getActivity().findViewById(R.id.position_thisweek);
            tv_position_thisweek.setText(pos_thisweek);

            TextView tv_your_best_alltime = (TextView) getActivity().findViewById(R.id.your_best_alltime);
            tv_your_best_alltime.setText(time_alltime);
            TextView tv_position_alltime = (TextView) getActivity().findViewById(R.id.position_alltime);
            tv_position_alltime.setText(pos_alltime);

            TextView tv_global_today = (TextView) getActivity().findViewById(R.id.global_best_today);
            tv_global_today.setText(global_top_today);

            TextView tv_global_thisweek = (TextView) getActivity().findViewById(R.id.global_best_thisweek);
            tv_global_thisweek.setText(global_top_thisweek);

            TextView tv_global_alltime = (TextView) getActivity().findViewById(R.id.global_best_alltime);

            tv_global_alltime.setText(global_top_alltime);
            getActivity().findViewById(R.id.hs_title_ll).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            getActivity().findViewById(R.id.hs_today_ll).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            getActivity().findViewById(R.id.hs_thisweek_ll).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            getActivity().findViewById(R.id.hs_alltime_ll).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            getActivity().findViewById(R.id.show_leaderboards_button).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            getActivity().findViewById(R.id.refresh_btn).setVisibility(loggedIn ?
                    View.VISIBLE : View.GONE);
            TextView tv_global_title = (TextView) getActivity().findViewById(R.id.hs_global_title);
            tv_global_title.setText(loggedIn ? getString(R.string.hs_global_title) : getString(R.string.hs_global_title_default));
        }
    }

    public void setTodayPlayerScores(long time, long position){
        time_today = formatTimeToString(time);
        pos_today = formatPosToString(position);
        updateUi();
    }
    public void setThisWeekPlayerScores(long time, long position){
        time_thisweek = formatTimeToString(time);
        pos_thisweek = formatPosToString(position);
        updateUi();
    }
    public void setAllTimePlayerScores(long time, long position){
        time_alltime = formatTimeToString(time);
        pos_alltime = formatPosToString(position);
        updateUi();
    }
    public void setTodayTopScore(long time){
        global_top_today = formatTimeToString(time);
        updateUi();
    }
    public void setThisWeekTopScore(long time){
        global_top_thisweek = formatTimeToString(time);
        updateUi();
    }
    public void setAllTimeTopScore(long time){
        global_top_alltime = formatTimeToString(time);
        updateUi();
    }
}
