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

/**
 * Created by Arttu on 21.3.2016.
 */
public class CustomResultDialog extends DialogFragment implements
        android.view.View.OnClickListener {

    public static final String SWITCH_TO_HIGHSCORE = "com.ardeapps.mazespeedrun.SWITCH_TO_HIGHSCORE";
    public Button highscores, again;
    public TextView nameText, timeText, bestTimeText;
    String map_name;
    Float time;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static CustomResultDialog newInstance(String name, Float finalTime) {
        CustomResultDialog f = new CustomResultDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
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
        highscores.setOnClickListener(this);
        again.setOnClickListener(this);

        nameText.setText(map_name);
        timeText.setText(time + " " + getString(R.string.seconds));
        bestTimeText.setText("asd");

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
        }
    }
}