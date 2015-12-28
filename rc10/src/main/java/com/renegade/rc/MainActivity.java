package com.renegade.rc;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentManager;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    public static final int STATUS = 0, TEAMSPEAK = 1, CYCLE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_api_10);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer_api_10,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
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

}