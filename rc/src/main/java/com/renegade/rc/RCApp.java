package com.renegade.rc;

import android.app.Activity;
import android.content.Intent;
//import android.os.Build;
import android.os.Bundle;
//import android.os.PersistableBundle;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.Tracker;

//import java.util.HashMap;

public class RCApp extends Activity {

    // The following line should be changed to include the correct property id.
//    private static final String PROPERTY_ID = "UA-58043202-1";

    // Logging TAG
//    private static final String TAG = "RC";

//    public static int GENERAL_TRACKER = 0;

//    public enum TrackerName {
//        APP_TRACKER, // Tracker used only in this app.
//        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
//    }

//    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

//    public RCApp() {
//        super();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = new Intent(this, MainActivity_Tabs.class);
//        }

        startActivity(intent);
        finish();
    }

//    synchronized Tracker getTracker(TrackerName trackerId) {
//        if (!mTrackers.containsKey(trackerId)) {
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.tracker)
//                    : analytics.newTracker(PROPERTY_ID);
//            mTrackers.put(trackerId, t);
//        }
//
//        return mTrackers.get(trackerId);
//    }

}