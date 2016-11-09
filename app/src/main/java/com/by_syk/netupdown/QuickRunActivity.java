package com.by_syk.netupdown;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.by_syk.netupdown.service.NetTrafficService;

/**
 * Created by By_syk on 2016-11-09.
 */

public class QuickRunActivity extends Activity {
    private final static String ACTION_RUN = "com.by_syk.unicode.ACTION_RUN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ACTION_RUN.equals(getIntent().getAction())) {
            if (!NetTrafficService.isRunning) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putBoolean("run", true).commit();
                startService(new Intent(this, NetTrafficService.class));
            }
        }

        finish();
    }
}
