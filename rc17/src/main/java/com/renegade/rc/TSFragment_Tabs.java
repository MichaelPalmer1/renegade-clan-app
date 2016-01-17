package com.renegade.rc;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;

public class TSFragment_Tabs extends Fragment {
	private WebView tsViewer;
    private MenuItem btn_refresh;
	private SwipeRefreshLayout refreshTS;
	private ScrollView scrollTS;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}
	
	@Override @SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_ts, container, false);

		// TS Web View
		tsViewer = (WebView) rootView.findViewById(R.id.tsViewer);
		tsViewer.setOnKeyListener(new WebView.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return false;
			}
		});
		tsViewer.getSettings().setJavaScriptEnabled(true);
		tsViewer.loadUrl("file:///android_asset/ts.html");

		// Swipe Refresh Layout
		refreshTS = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshTS);
		// Set on refresh listener for swipe refresh
		refreshTS.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				doRefresh();
			}
		});
		refreshTS.setColorSchemeColors(Color.BLUE, Color.GREEN);

		// Scroll view
		scrollTS = (ScrollView) rootView.findViewById(R.id.scrollTsViewer);

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
			doRefresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void tabSelected() {
		scrollTS.fullScroll(View.FOCUS_UP);
		doRefresh();
	}

	private void doRefresh() {
		btn_refresh.setEnabled(false);
		refreshTS.setRefreshing(true);
		tsViewer.loadUrl("file:///android_asset/ts.html");
		new Handler().postDelayed(new Runnable() {
			public void run() {
				refreshTS.setRefreshing(false);
				btn_refresh.setEnabled(true);
			}
		}, 1800);
	}
}