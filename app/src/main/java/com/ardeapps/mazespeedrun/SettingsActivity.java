package com.ardeapps.mazespeedrun;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Arttu on 14.3.2016.
 */
public class SettingsActivity extends Activity {

    SharedPreferences appPref;
    String username = "";
    String country;
    EditText usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        usernameText = (EditText) findViewById(R.id.username);
        appPref = getSharedPreferences("username", 0);
        username = appPref.getString("username", null);

        try{
            if (!username.equals("")) {
                usernameText.setText(username);
            }
        } catch (NullPointerException ex){
            Log.e("ERROR", "nullpointer" + ex);
        }

    }

    public void saveAndPlay(View v){
        Toast.makeText(getApplicationContext(), R.string.saved, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = appPref.edit();
        editor.putString("username", usernameText.getText().toString());
        editor.apply();

        finish();
    }
}
