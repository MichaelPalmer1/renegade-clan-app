package com.renegade.rc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;

public class TSFragment extends Fragment {
	private WebView tsViewer;
    private MenuItem btn_refresh;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}

//    @Override
//    public void onStart() {
//        super.onStart();
//        ((RCApp) getActivity().getApplication())
//                .getTracker(RCApp.TrackerName.APP_TRACKER)
//                .setScreenName("com.renegade.rc.TSFragment");
//
//        ((RCApp) getActivity().getApplication())
//                .getTracker(RCApp.TrackerName.APP_TRACKER)
//                .send(new HitBuilders.AppViewBuilder().build());
//    }
	
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
//            sendEvent(R.string.categoryTS, R.string.actionRefresh);
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

//    public void sendEvent(int categoryId, int actionId) {
//        Tracker t = ((RCApp) getActivity().getApplication()).getTracker(
//                RCApp.TrackerName.APP_TRACKER);
//
//        t.send(new HitBuilders.EventBuilder()
//                .setCategory(getString(categoryId))
//                .setAction(getString(actionId))
//                .build());
//    }

}