package com.renegade.rc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RCApp extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}