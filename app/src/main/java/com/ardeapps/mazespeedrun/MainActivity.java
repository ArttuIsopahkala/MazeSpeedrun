package com.ardeapps.mazespeedrun;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import java.util.Locale;

public class MainActivity extends FragmentActivity implements
        MainFragment.Listener, HighscoreFragment.Listener, MazeFragment.Listener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

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
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "TanC";

    boolean mShowSignIn = true;

    // create intent filter for mainFragment, switching highscores or start gameplay
    IntentFilter filter = new IntentFilter();
    /** MENU ITEMS */
    private static final int MENU_ITEM_ID_ABOUT =1;
    private static final int MENU_ITEM_ID_LOGIN =2;
    private static final int MENU_ITEM_ID_LEADERBOARDS =3;
    private static final int MENU_ITEM_ID_LOGOUT =4;
    private int mMenuSet = 1;

    /** DATABASE, MAP AND RESULT VARIABLES */
    Float finalTime = -1f;
    String maze_name;
    public SQLiteDatabase db;
    String nameInCloud;
    long timeInCloud;
    Uri urlimageInCloud;
    long positionInCloud;

    /** BROADCASTRECIEVER FROM MazeAdapter for onclick events in map list*/
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
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
        mainFragment.setListener(this);
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
        //getHighscoresFromCloud();

        // load local times from database and update
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
            menu.add(Menu.NONE, MENU_ITEM_ID_LEADERBOARDS, Menu.NONE,getString(R.string.action_leaderboards)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, MENU_ITEM_ID_LOGOUT, Menu.NONE, getString(R.string.action_logout)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
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
        mGoogleApiClient.connect();
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
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.COLLECTION_PUBLIC), RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }

    @Override
    public void onGameFinished(Float requestedTime, String maze_name) {
        // update leaderboards
        this.maze_name = maze_name;
        this.finalTime = requestedTime; //to milliseconds for ex. 1333

        long timeToCloud = (long)(requestedTime*1000);
        int leaderboardId = mazeData.getMapId(maze_name);

        Log.d(TAG, finalTime+" final time");
        Log.d(TAG, timeToCloud + " timeToCloud");
        Log.d(TAG, "uusi ennätys? "+(mazeData.getMapTime(maze_name) == -1 || mazeData.getMapTime(maze_name) > timeToCloud));
        Log.d(TAG, mazeData.getMapTime(maze_name) + " getmapTime");

        //if time is first result, or best time
        if (mazeData.getMapTime(maze_name) == -1 || mazeData.getMapTime(maze_name) > timeToCloud) {
            mazeData.setNewMapTime(maze_name, timeToCloud);
            Log.d(TAG, mazeData.getMapTime(maze_name) + " NewgetmapTime");
            // push those accomplishments to the cloud, if signed in
            if (isSignedIn() && leaderboardId > 0) {
                Log.d(TAG, "222");
                Games.Leaderboards.submitScore(mGoogleApiClient, getString(leaderboardId),
                        mazeData.getMapTime(maze_name));
            }
        }

        mazeData.saveLocal(db, finalTime, maze_name);
    }

    /** push to cloud if player has played without signed in and then signs in */
    public void pushToCloud(){
        if (!mazeData.isEmpty()) {
            for(MazeData.Maze maze : mazes){
                getPlayerHighScoreFromCloud(maze.maze_name);
                Log.d(TAG, maze.mapTime + " mapTime");
                if(maze.mapTime > 0 && maze.mapTime < timeInCloud){
                    Log.d(TAG, "submitted");
                    Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded), Toast.LENGTH_LONG).show();
                    Games.Leaderboards.submitScore(mGoogleApiClient, getString(maze.mapId),
                            mazeData.getMapTime(maze.maze_name));
                }
            }
        }
    }

    public void getPlayerHighScoreFromCloud(String map_name){
        int leaderboardId = mazeData.getMapId(map_name);
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(leaderboardId), LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                if (isPlayerScoreResultValid(arg0)) {
                    LeaderboardScore lbs = arg0.getScore();
                    timeInCloud = lbs.getRawScore();
                    positionInCloud = lbs.getRank();

                    Log.d(TAG, timeInCloud + " score");
                    //Log.d(TAG, positionInCloud + " displayrank");
                } else timeInCloud = 99999999;
            }
        });
    }

    public void getHighscoresFromCloud(){
        Games.Leaderboards.loadTopScores(mGoogleApiClient, getString(R.string.leaderboard_map_1),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC, 5).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {

            public void onResult(Leaderboards.LoadScoresResult arg0) {
                int size = arg0.getScores().getCount();
                if (size <= 5) {
                    for (int i = 0; i < size; i++) {
                        if (isScoreResultValid(arg0)) {
                            LeaderboardScore lbs = arg0.getScores().get(i);
                            String name = lbs.getScoreHolderDisplayName();
                            String score = lbs.getDisplayScore();
                            Uri urlimage = lbs.getScoreHolderHiResImageUri();

                            Log.d(TAG, size + " size");
                            Log.d(TAG, name + " name");
                            Log.d(TAG, score + " score");
                            Log.d(TAG, urlimage + " image");
                            Log.d(TAG, lbs.getDisplayRank() + " displayrank");
                        }
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                        if (isScoreResultValid(arg0)) {
                            LeaderboardScore lbs = arg0.getScores().get(i);
                            String name = lbs.getScoreHolderDisplayName();
                            String score = lbs.getDisplayScore();
                            Uri urlimage = lbs.getScoreHolderHiResImageUri();

                            Log.d(TAG, size + " size");
                            Log.d(TAG, name + " name");
                            Log.d(TAG, score + " score");
                            Log.d(TAG, urlimage + " image");
                        }
                    }
                }
                //arg0.getScores().close();
            }
        });
    }

    private boolean isPlayerScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    private boolean isScoreResultValid(final Leaderboards.LoadScoresResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScores() != null;
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

        // if we have accomplishments to push, push them
        //pushToCloud();
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
    }

}