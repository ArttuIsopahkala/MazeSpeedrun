package com.ardeapps.mazespeedrun;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by Arttu on 21.3.2016.
 * DialogFragment which appears after finishing maze
 */
public class CustomResultDialog extends DialogFragment implements
        android.view.View.OnClickListener {

    public static final String SWITCH_TO_HIGHSCORE = "com.ardeapps.mazespeedrun.SWITCH_TO_HIGHSCORE";
    public Button highscores, again, menu;
    public TextView nameText, timeText, bestTimeText;
    String map_name;
    String best_time;
    Float time;
    boolean isNewHighscore;
    // Ads
    InterstitialAd mInterstitialAd;

    static CustomResultDialog newInstance(String name, Float finalTime, String bestTime, boolean newBest) {
        CustomResultDialog f = new CustomResultDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("best_time", bestTime);
        args.putBoolean("new_best", newBest);
        //Check if not finished right
        if(finalTime != 0){
            args.putFloat("time", finalTime);
        }
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map_name = getArguments().getString("name", "Map");
        time = getArguments().getFloat("time", 0.00f);
        best_time = getArguments().getString("best_time", "-");
        isNewHighscore = getArguments().getBoolean("new_best", false);
        // Load ads
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();

            }
        });
        requestNewInterstitial();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_result, container);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);


        timeText = (TextView) v.findViewById(R.id.time);
        bestTimeText = (TextView) v.findViewById(R.id.time_best);
        nameText = (TextView) v.findViewById(R.id.map_name);
        highscores = (Button) v.findViewById(R.id.btn_highscores);
        again = (Button) v.findViewById(R.id.btn_again);
        menu = (Button) v.findViewById(R.id.btn_menu);
        highscores.setOnClickListener(this);
        again.setOnClickListener(this);
        menu.setOnClickListener(this);

        nameText.setText(map_name);

        if(time == 0){
            timeText.setText(getString(R.string.not_finish));
        }else timeText.setText(getString(R.string.your_time)+ ": " + time + "s");

        Log.d("resultdialog", "best_time: " + best_time);
        Log.d("resultdialog", "new highscrore: " + isNewHighscore);
        if(best_time.contains("0.00")){
            bestTimeText.setText(getString(R.string.your_best)+": -");
        }else if(isNewHighscore){
            bestTimeText.setText(getString(R.string.new_highscore));
        }else bestTimeText.setText(getString(R.string.your_best)+": "+best_time+"s");


        return v;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_highscores:
                Bundle mBundle = new Bundle();
                mBundle.putString("name", map_name);
                Intent hsIntent = new Intent(SWITCH_TO_HIGHSCORE);
                hsIntent.putExtras(mBundle);
                getActivity().sendBroadcast(hsIntent);
                dismiss();
                break;
            case R.id.btn_again:
                dismiss();
                break;
            case R.id.btn_menu:
                getActivity().getSupportFragmentManager().popBackStack();
                dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                break;
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9C719002557124CD0CCB65412A2C3EE1")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}