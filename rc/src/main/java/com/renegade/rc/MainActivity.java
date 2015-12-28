package com.renegade.rc;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentManager;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;

//import com.google.android.gms.analytics.GoogleAnalytics;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    public static final int STATUS = 0, TEAMSPEAK = 1, CYCLE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

//        ((RCApp) getApplication()).getTracker(RCApp.TrackerName.APP_TRACKER);
    }

//    @Override
//    protected void onStart() {
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
//        super.onStart();
//    }

//    @Override
//    protected void onStop() {
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
//        super.onStop();
//    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch(position) {
            case STATUS: // Server Status
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new StatusFragment())
                        .commit();
                break;
        	case TEAMSPEAK: // TeamSpeak
        		fragmentManager.beginTransaction()
                	.replace(R.id.container, new TSFragment())
                	.commit();
        		break;
        	case CYCLE: // Map Cycle
        		fragmentManager.beginTransaction()
                	.replace(R.id.container, new MapCycleFragment())
                	.commit();
        		break;
        	default: // Server Status
        		fragmentManager.beginTransaction()
                	.replace(R.id.container, new StatusFragment())
                	.commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case STATUS:    mTitle = getString(R.string.title_section1); break;
            case TEAMSPEAK: mTitle = getString(R.string.title_section2); break;
            case CYCLE:     mTitle = getString(R.string.title_section3); break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
        	MenuInflater inflater = getMenuInflater();
        	inflater.inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//        	return false;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}