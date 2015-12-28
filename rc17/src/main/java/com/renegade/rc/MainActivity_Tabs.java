package com.renegade.rc;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity_Tabs extends FragmentActivity implements ActionBar.TabListener {

    public static final int STATUS = 0, TEAMSPEAK = 1, CYCLE = 2;

    private static StatusFragment_Tabs status = null;
    private static TSFragment_Tabs ts = null;
    private static MapCycleFragment_Tabs cycle = null;
    private ViewPager viewPager = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tabs);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        if (actionBar == null)
            return;

        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        PageAdapter pagerAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//        switch (tab.getPosition()) {
//            case STATUS:
//                fragmentTransaction.hide(status);
//                break;
//
//            case TEAMSPEAK:
//                fragmentTransaction.hide(ts);
//                break;
//
//            case CYCLE:
//                fragmentTransaction.hide(cycle);
//                break;
//        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
//        switch (tab.getPosition()) {
//            case STATUS:
//                if (status == null) {
//                    status = new StatusFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, status);
//                } else {
//                    fragmentTransaction.show(status);
//                }
//                break;
//
//            case TEAMSPEAK:
//                if (ts == null) {
//                    ts = new TSFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, ts);
//                } else {
//                    fragmentTransaction.show(ts);
//                }
//                break;
//
//            case CYCLE:
//                if (cycle == null) {
//                    cycle = new MapCycleFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, cycle);
//                } else {
//                    fragmentTransaction.show(cycle);
//                }
//                break;
//        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case STATUS:
                    if (status == null)
                        status = new StatusFragment_Tabs();
                    return status;

                case TEAMSPEAK:
                    if (ts == null)
                        ts = new TSFragment_Tabs();
                    return ts;
                case CYCLE:
                    if (cycle == null)
                        cycle = new MapCycleFragment_Tabs();
                    return cycle;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case STATUS:    return getString(R.string.title_section1);
                case TEAMSPEAK: return getString(R.string.title_section2);
                case CYCLE:     return getString(R.string.title_section3);
            }
            return null;
        }
    }
}