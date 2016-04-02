package com.ardeapps.mazespeedrun;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

public class MainActivity extends FragmentActivity implements
        MainFragment.Listener, HighscoreFragment.Listener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    // Fragments
    MainFragment mainFragment;
    HighscoreFragment highscoreFragment;

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

    // playing on hard mode?
    boolean mHardMode = false;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    boolean mShowSignIn = true;

    // create intent filter
    IntentFilter filter = new IntentFilter();

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

        mainFragment = new MainFragment();
        highscoreFragment = new HighscoreFragment();
        mainFragment.setListener(this);
        highscoreFragment.setListener(this);

        filter.addAction(MazeAdapter.SWITCH_TO_HIGHSCORE);
        filter.addAction(MazeAdapter.SWITCH_TO_MAZE);

        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFragment)
                .commit();


        // load outbox from file
        mOutbox.loadLocal(this);
    }

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
                    break;
            }
        }
    };

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
    public void onBackPressed() {
        //go to previous fragment or close app if user is on mainscreen
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
            getSupportFragmentManager().popBackStack();
        } else super.onBackPressed();
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
    public void onStartGameRequested(boolean hardMode) {
        //TODO valitse kenttä täällä ja intentti sinne fragmenttiin

    }

    public void onShowLeaderboardsRequested() {

        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            Toast.makeText(this.getApplicationContext(), "ei onnistu ", Toast.LENGTH_LONG).show();
            //BaseGameUtils.makeSimpleDialog(asd, getString(R.string.leaderboards_not_available)).show();
        }
    }

    public void onEnteredScore(int requestedScore) {
        // update leaderboards
        updateLeaderboards(requestedScore);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();

        // switch to the exciting "you won" screen
        //switchToFragment(mWinFragment);
    }

    void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(this);
            return;
        }

        if (mOutbox.mHardModeScore >= 0) {
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_map_1),
                    mOutbox.mHardModeScore);
            mOutbox.mHardModeScore = -1;
        }
        mOutbox.saveLocal(this);
    }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    void updateLeaderboards(int finalScore) {
        if (mHardMode && mOutbox.mHardModeScore < finalScore) {
            mOutbox.mHardModeScore = finalScore;
        } else if (!mHardMode && mOutbox.mEasyModeScore < finalScore) {
            mOutbox.mEasyModeScore = finalScore;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handles item selections
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                this.startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                this.startActivity(infoIntent);
                return true;
        }
        return false;
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
        // Show sign-out button on main menu
        mainFragment.setShowSignInButton(false);


        // Show "you are signed in" message on win screen, with no sign in button.
        //mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        mainFragment.setGreeting("Hello, " + displayName);

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
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
            Log.d("TanC", "ERRERMSG");
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
        mainFragment.setShowSignInButton(true);
        //highScoreFragment.setShowSignInButton(true);

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
        }

        mainFragment.setGreeting(getString(R.string.default_greeting));
        mainFragment.setShowSignInButton(true);
        //highScoreFragment.setShowSignInButton(true);
    }

    class AccomplishmentsOutbox {
        boolean mPrimeAchievement = false;
        boolean mHumbleAchievement = false;
        boolean mLeetAchievement = false;
        boolean mArrogantAchievement = false;
        int mBoredSteps = 0;
        int mEasyModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty() {
            return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
                    mHardModeScore < 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }

}