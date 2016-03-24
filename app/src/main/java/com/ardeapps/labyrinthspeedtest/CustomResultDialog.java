package com.ardeapps.labyrinthspeedtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Arttu on 21.3.2016.
 */
public class CustomResultDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button highscores, again;
    public TextView nameText, timeText, bestTimeText;
    String map_name;
    String time;

    public CustomResultDialog(Activity a, String map_name, String time) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.map_name = map_name;
        this.time = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_result);
        timeText = (TextView) findViewById(R.id.time);
        bestTimeText = (TextView) findViewById(R.id.time_best);
        nameText = (TextView) findViewById(R.id.map_name);
        highscores = (Button) findViewById(R.id.btn_highscores);
        again = (Button) findViewById(R.id.btn_again);
        highscores.setOnClickListener(this);
        again.setOnClickListener(this);

        nameText.setText(map_name);
        timeText.setText(time+" "+c.getString(R.string.seconds));
        bestTimeText.setText("asd");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_highscores:
                Intent myIntent = new Intent(c, HighscoreActivity.class);
                myIntent.putExtra("name", map_name);
                c.startActivity(myIntent);
                break;
            case R.id.btn_again:
                dismiss();
                break;
            default:
                break;
        }
    }
}