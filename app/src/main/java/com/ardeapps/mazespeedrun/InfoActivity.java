package com.ardeapps.mazespeedrun;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView versionTextView = (TextView) findViewById(R.id.version);
        TextView rateTextView = (TextView) findViewById(R.id.rate);
        TextView moreTextView = (TextView) findViewById(R.id.more);
        versionTextView.setText(versionTextView.getText().toString()+" "+BuildConfig.VERSION_NAME);
        rateTextView.setText(
                Html.fromHtml(
                        "<a href=\"http://play.google.com/store/apps/details?id=com.ardeapps.sarjakuvalukija\">"+getText(R.string.info_link_rate)+"</a> "));
        rateTextView.setMovementMethod(LinkMovementMethod.getInstance());

        moreTextView.setText(
                Html.fromHtml(
                        "<a href=\"http://play.google.com/store/apps/developer?id=Arde+Apps\">" + getText(R.string.info_link_more) + "</a> "));
        moreTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
