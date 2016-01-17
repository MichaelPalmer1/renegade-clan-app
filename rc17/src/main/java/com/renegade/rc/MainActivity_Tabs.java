package com.renegade.rc;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity_Tabs extends FragmentActivity implements ActionBar.TabListener {

    public static final int STATUS = 0, TS = 1, CYCLE = 2, BANS = 3, WARNINGS = 4;

    private static StatusFragment_Tabs_Cards status = null;
    private static TSFragment_Tabs ts = null;
    private static MapCycleFragment_Tabs_Cards cycle = null;
    private static BansFragment_Tabs_Cards bans = null;
    private static WarningsFragment_Tabs_Cards warnings = null;
    private ViewPager viewPager = null;
    private int lastTab = STATUS;

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
        lastTab = tab.getPosition();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
        lastTab = tab.getPosition();
        /*
        switch (tab.getPosition()) {
            case STATUS:
//                if (status == null) {
//                    status = new StatusFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, status);
//                } else {
//                    fragmentTransaction.show(status);
//                }
                break;

            case TS:
//                if (ts == null) {
//                    ts = new TSFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, ts);
//                } else {
//                    fragmentTransaction.show(ts);
//                }
                break;

            case CYCLE:
//                if (cycle == null) {
//                    cycle = new MapCycleFragment_Tabs();
//                    fragmentTransaction.add(R.id.pager, cycle);
//                } else {
//                    fragmentTransaction.show(cycle);
//                }
                break;
        }
        */
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if(tab.getPosition() == lastTab) {
            switch (tab.getPosition()) {
                case STATUS:
                    status.tabSelected();
                    break;

                case TS:
                    ts.tabSelected();
                    break;

                case CYCLE:
                    cycle.tabSelected();
                    break;

                case BANS:
                    bans.tabSelected();
                    break;

                case WARNINGS:
                    warnings.tabSelected();
                    break;
            }
        }
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
                        status = new StatusFragment_Tabs_Cards();
                    return status;

                case TS:
                    if (ts == null)
                        ts = new TSFragment_Tabs();
                    return ts;

                case CYCLE:
                    if (cycle == null)
                        cycle = new MapCycleFragment_Tabs_Cards();
                    return cycle;

                case BANS:
                    if (bans == null)
                        bans = new BansFragment_Tabs_Cards();
                    return bans;

                case WARNINGS:
                    if (warnings == null)
                        warnings = new WarningsFragment_Tabs_Cards();
                    return warnings;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case STATUS:    return getString(R.string.title_section1);
                case TS:        return getString(R.string.title_section2);
                case CYCLE:     return getString(R.string.title_section3);
                case BANS:      return getString(R.string.title_section4);
                case WARNINGS:  return getString(R.string.title_section5);
            }
            return null;
        }
    }
}