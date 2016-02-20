package com.renegade.rc;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.ArrayList;

public class MainActivity_Tabs extends AppCompatActivity {

    private static int lastTab = 0;
    private static String[] titles;
    private static ArrayList<RCFragment> fragments = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);

        // Set up the action bar.
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // Setup fragments
        titles = getResources().getStringArray(R.array.section_titles);
        String[] fragmentClasses = getResources().getStringArray(R.array.section_classes);
        for (String fragment : fragmentClasses) {
            fragments.add((RCFragment) RCFragment
                    .instantiate(this, getPackageName() + "." + fragment));
        }

        // Set up the view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);

        // Add fragments
        ViewAdapter adapter = new ViewAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // Set up the tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new ViewTabSelected(viewPager));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    static class ViewAdapter extends FragmentPagerAdapter {

        public ViewAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public RCFragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    static class ViewTabSelected extends TabLayout.ViewPagerOnTabSelectedListener {

        public ViewTabSelected(ViewPager viewPager) {
            super(viewPager);
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            super.onTabSelected(tab);
            lastTab = tab.getPosition();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            super.onTabUnselected(tab);
            lastTab = tab.getPosition();
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            super.onTabReselected(tab);
            if(tab.getPosition() == lastTab) {
                fragments.get(tab.getPosition()).tabSelected();
            }
        }
    }
}

abstract class RCFragment extends Fragment {
    public void tabSelected() {}
}