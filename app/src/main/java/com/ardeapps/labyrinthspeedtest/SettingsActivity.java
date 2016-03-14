package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
        finish();
    }
}
