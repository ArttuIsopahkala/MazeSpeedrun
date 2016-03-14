package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    // Create Preference to check if application is going to be called first
    // time.
    SharedPreferences appPref;
    boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get preference value to know that is it first time application is
        // being called.
        appPref = getSharedPreferences("isFirstTime", 0);
        isFirstTime = appPref.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            SharedPreferences.Editor editor = appPref.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
            finish();
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
                break;
            case R.id.action_about:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                this.startActivity(infoIntent);
                break;
        }
        return false;
    }
}
