package com.ardeapps.mazespeedrun;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {


    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        TextView versionTextView = (TextView) v.findViewById(R.id.version);
        TextView rateTextView = (TextView) v.findViewById(R.id.rate);
        TextView moreTextView = (TextView) v.findViewById(R.id.more);
        versionTextView.setText(versionTextView.getText().toString()+" "+BuildConfig.VERSION_NAME);
        rateTextView.setText(
                Html.fromHtml(
                        "<a href=\"http://play.google.com/store/apps/details?id=com.ardeapps.sarjakuvalukija\">" + getText(R.string.info_link_rate) + "</a> "));
        rateTextView.setMovementMethod(LinkMovementMethod.getInstance());

        moreTextView.setText(
                Html.fromHtml(
                        "<a href=\"http://play.google.com/store/apps/developer?id=Arde+Apps\">" + getText(R.string.info_link_more) + "</a> "));
        moreTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }


}
