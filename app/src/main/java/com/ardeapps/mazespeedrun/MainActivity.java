package com.ardeapps.mazespeedrun;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements
        HighscoreFragment.Listener, MazeFragment.Listener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DialogInterface.OnDismissListener {

    // Fragments
    MainFragment mainFragment;
    HighscoreFragment highscoreFragment;
    MazeFragment mazeFragment;
    InfoFragment infoFragment;
    MazeData mazeData = new MazeData();
    ArrayList<MazeData.Maze> mazes;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    final String TAG = "TanC";

    // create intent filter for mainFragment, switching highscores or start gameplay
    IntentFilter filter = new IntentFilter();
    /** MENU ITEMS */
    private static final int MENU_ITEM_ID_ABOUT =1;
    private static final int MENU_ITEM_ID_LOGIN =2;
    private static final int MENU_ITEM_ID_ACHIEVEMENTS =3;
    private static final int MENU_ITEM_ID_LEADERBOARDS =4;
    private static final int MENU_ITEM_ID_LOGOUT =5;
    private int mMenuSet = 1;

    /** DATABASE, MAP AND RESULT VARIABLES */
    Float finalTime = -1f;
    String maze_name;
    public SQLiteDatabase db;

    /** BROADCASTRECIEVER FROM MazeAdapter for onclick events on map list or from customResultDialog*/
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            switch(intent.getAction()){
                case MazeAdapter.SWITCH_TO_HIGHSCORE:
                    highscoreFragment.setArguments(bundle);
                    switchToFragment(highscoreFragment);
                    break;
                case MazeAdapter.SWITCH_TO_MAZE:
                    mazeFragment.setArguments(bundle);
                    switchToFragment(mazeFragment);
                    break;
            }
        }
    };

    public void getGlobalHighscores(int leaderboardId){
        if(!isNetworkAvailable()){
            Toast.makeText(MainActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
        } else if(isSignedIn()) {
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(getString(R.string.loading_title));
            progress.setMessage(getString(R.string.loading_desc));
            progress.setCancelable(false);
            progress.show();

            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.TIME_SPAN_DAILY,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                    if (isPlayerScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScore();
                        long time = lbs.getRawScore();
                        long pos = lbs.getRank();
                        highscoreFragment.setTodayPlayerScores(time, pos);
                    }
                }
            });

            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.TIME_SPAN_WEEKLY,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                    if (isPlayerScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScore();
                        long time = lbs.getRawScore();
                        long pos = lbs.getRank();
                        highscoreFragment.setThisWeekPlayerScores(time, pos);
                    }
                }
            });
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                    if (isPlayerScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScore();
                        long time = lbs.getRawScore();
                        long pos = lbs.getRank();
                        highscoreFragment.setAllTimePlayerScores(time, pos);
                    }
                }
            });
            Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(leaderboardId),
                    LeaderboardVariant.TIME_SPAN_DAILY,
                    LeaderboardVariant.COLLECTION_PUBLIC, 1).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {

                public void onResult(Leaderboards.LoadScoresResult arg0) {
                    if (isScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScores().get(0);
                        long time = lbs.getRawScore();
                        highscoreFragment.setTodayTopScore(time);
                    }
                }
            });
            Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(leaderboardId),
                    LeaderboardVariant.TIME_SPAN_WEEKLY,
                    LeaderboardVariant.COLLECTION_PUBLIC, 1).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {

                public void onResult(Leaderboards.LoadScoresResult arg0) {
                    if (isScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScores().get(0);
                        long time = lbs.getRawScore();
                        highscoreFragment.setThisWeekTopScore(time);
                    }
                }
            });
            Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(leaderboardId),
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC, 1).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {

                public void onResult(Leaderboards.LoadScoresResult arg0) {
                    if (isScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScores().get(0);
                        long time = lbs.getRawScore();
                        highscoreFragment.setAllTimeTopScore(time);
                    }

                    // To dismiss the dialog
                    progress.dismiss();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Google API Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        //initialize mazedata
        mazes =  mazeData.getMazeData();

        infoFragment = new InfoFragment();
        mainFragment = new MainFragment();
        highscoreFragment = new HighscoreFragment();
        mazeFragment = new MazeFragment();
        highscoreFragment.setListener(this);
        mazeFragment.setListener(this);

        filter.addAction(MazeAdapter.SWITCH_TO_HIGHSCORE);
        filter.addAction(MazeAdapter.SWITCH_TO_MAZE);

        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFragment)
                .commit();

        // get database instance
        db = (new Database(this)).getWritableDatabase();

        mazeData.loadLocal(db);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(mMenuSet==1){
            menu.add(Menu.NONE, MENU_ITEM_ID_LOGIN, Menu.NONE, getString(R.string.action_login)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ITEM_ID_ABOUT, Menu.NONE, getString(R.string.action_about)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }else if(mMenuSet==2){
            menu.add(Menu.NONE, MENU_ITEM_ID_ABOUT, Menu.NONE,getString(R.string.action_about)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, MENU_ITEM_ID_ACHIEVEMENTS, Menu.NONE,getString(R.string.action_achievements)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, MENU_ITEM_ID_LEADERBOARDS, Menu.NONE,getString(R.string.action_leaderboards)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, MENU_ITEM_ID_LOGOUT, Menu.NONE, getString(R.string.action_logout)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        // Style overflow items text color
        for(int i = 0; i < menu.size(); i++){
            MenuItem menuItem = menu.getItem(i);
            CharSequence menuTitle = menuItem.getTitle();
            SpannableString styledMenuTitle = new SpannableString(menuTitle);
            styledMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_text_light)), 0, menuTitle.length(), 0);
            menuItem.setTitle(styledMenuTitle);
        }

        return true;
    }

    // Handles item selections
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ID_ABOUT:
                switchToFragment(infoFragment);
                break;
            case MENU_ITEM_ID_LOGIN:
                onSignInButtonClicked();
                break;
            case MENU_ITEM_ID_ACHIEVEMENTS:
                if (isSignedIn()) {
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                            RC_UNUSED);
                } else {
                    BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
                }
                break;
            case MENU_ITEM_ID_LEADERBOARDS:
                if (isSignedIn()) {
                    startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                            RC_UNUSED);
                } else {
                    BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
                }
                break;
            case MENU_ITEM_ID_LOGOUT:
                onSignOutButtonClicked();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        if(isNetworkAvailable()){
            mGoogleApiClient.connect();
        }
        registerReceiver(mBroadcastReceiver, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onDestroy(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        unregisterReceiver(mBroadcastReceiver);
    }
    @Override
    public void onBackPressed() {
        //go to previous fragment or close app if user is on mainscreen
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
            getSupportFragmentManager().popBackStack();
        } else super.onBackPressed();
    }
    @Override
    public void onDismiss(final DialogInterface dialog) {
        //Fragment dialog had been dismissed
        mazeFragment.initializeGame();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newFrag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onShowLeaderboardsRequested(String map_name) {
        int leaderboardId = mazeData.getMapId(map_name);
        if (isSignedIn() && isNetworkAvailable()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.COLLECTION_PUBLIC), RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.no_connection)).show();
        }
    }

    @Override
    public void onGetGlobalHighscoresRequest(String map_name) {
        int leaderboardId = mazeData.getMapId(map_name);
        getGlobalHighscores(leaderboardId);
    }

    @Override
    public void onGameFinished(Float requestedTime, final String maze_name) {
        //update unlocked maps
        mazeData.loadLocal(db);

        // update leaderboards
        this.maze_name = maze_name;
        this.finalTime = requestedTime; //to milliseconds for ex. 1333

        final long timeToCloud = (long)(requestedTime*1000);
        final int leaderboardId = mazeData.getMapId(maze_name);

        if (isSignedIn() && leaderboardId > 0) {
            // Check achievements
            if(timeToCloud < 1000){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_time_under_1_second));
            }
            if(timeToCloud < 500){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_time_under_05_second));
            }
            if(timeToCloud < 300){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_time_under_03_second));
            }

            if(mazeData.getUnlockedCount() >= 10){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_all_easy_mazes_unlocked));
            }
            if(mazeData.getUnlockedCount() >= 20){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_all_medium_mazes_unlocked));
            }
            if(mazeData.getUnlockedCount() >= 30){
                Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_all_mazes_unlocked));
            }
            Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(leaderboardId),
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC, 1).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
                public void onResult(Leaderboards.LoadScoresResult arg0) {
                    if (isScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScores().get(0);
                        long time = lbs.getRawScore();
                        if(timeToCloud < time) {
                            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_all_time_global_highscore));
                        }
                    } else Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_all_time_global_highscore));
                }
            });
            Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(leaderboardId),
                    LeaderboardVariant.TIME_SPAN_DAILY,
                    LeaderboardVariant.COLLECTION_PUBLIC, 1).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {

                public void onResult(Leaderboards.LoadScoresResult arg0) {
                    if (isScoreResultValid(arg0)) {
                        LeaderboardScore lbs = arg0.getScores().get(0);
                        long time = lbs.getRawScore();
                        if(timeToCloud < time) {
                            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_5_global_highscores), 1);
                        }
                    } else Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_5_global_highscores), 1);
                }
            });
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_100_games_played), 1);
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_500_games_played), 1);

            //if time is first result, or best time set local best
            if (mazeData.getMapTime(maze_name) == -1 || mazeData.getMapTime(maze_name) > timeToCloud) {
                mazeData.setNewMapTime(maze_name, timeToCloud);
            }

            // Submit new score to cloud
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(leaderboardId), timeToCloud);
        }
        // Compare and save to local SQLite database
        mazeData.saveLocal(db, finalTime, maze_name);
    }
    @Override
    public void onUpdateBestTime(String maze_name) {
        long map_time = mazeData.getMapTime(maze_name);
        mazeFragment.setMapTime(map_time);
    }

    private boolean isPlayerScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    private boolean isScoreResultValid(final Leaderboards.LoadScoresResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScores() != null && scoreResult.getScores().getCount() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // Change different menu
        mMenuSet = 2;
        invalidateOptionsMenu();

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        mainFragment.setGreeting(getString(R.string.greeting) + ", " + displayName + "!");
        highscoreFragment.showGlobalScores(true);

        //if log in is pressed in highscoreFragment
        if(highscoreFragment.isVisible()){
            String mapName = highscoreFragment.getName();
            getGlobalHighscores(mazeData.getMapId(mapName));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
        // Sign-in failed, so show sign-in button on main activity
        mainFragment.setGreeting(getString(R.string.default_greeting));
        highscoreFragment.showGlobalScores(false);
    }

    @Override
    public void onSignInButtonClicked() {
        // start the sign-in flow
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onSignOutButtonClicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mMenuSet = 1;
            invalidateOptionsMenu();
        }
        mainFragment.setGreeting(getString(R.string.default_greeting));
        highscoreFragment.showGlobalScores(false);
    }

}