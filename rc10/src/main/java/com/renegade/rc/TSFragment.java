package com.renegade.rc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class TSFragment extends Fragment {
	private WebView tsViewer;
    private MenuItem btn_refresh;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}
	
	@Override @SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_ts, container, false);
		tsViewer = (WebView) rootView.findViewById(R.id.tsViewer);
		tsViewer.getSettings().setJavaScriptEnabled(true);
		tsViewer.loadUrl("file:///android_asset/ts.html");

		return rootView;
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        btn_refresh = menu.findItem(R.id.action_refresh);
        super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_refresh) {
            item.setEnabled(false);
			tsViewer.reload();

            // Enable refresh button
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    btn_refresh.setEnabled(true);
                }
            }, 1000);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(MainActivity.TEAMSPEAK);
    }

}