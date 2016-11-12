/**
 * Copyright 2016 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.by_syk.netupdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.by_syk.netupdown.service.NetTrafficService;

/**
 * Created by By_syk on 2016-11-08.
 */

public class PrefActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private CheckBoxPreference checkBoxPreference;

    private ServiceReceiver serviceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        checkBoxPreference = (CheckBoxPreference) findPreference("run");
        checkBoxPreference.setOnPreferenceChangeListener(this);

        serviceReceiver = new ServiceReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetTrafficService.ACTION_SERVICE_RUN);
        intentFilter.addAction(NetTrafficService.ACTION_SERVICE_DIED);
        registerReceiver(serviceReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkBoxPreference.isChecked() && !NetTrafficService.isRunning) {
            runService();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(serviceReceiver);
    }

    private void runService() {
        startService(new Intent(this, NetTrafficService.class));
    }

    private void stopService() {
        stopService(new Intent(this, NetTrafficService.class));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "run":
                boolean isChecked = (Boolean) newValue;
                if (isChecked) {
                    runService();
                } else {
                    stopService();
                }
                return false;
            default:
                return true;
        }
    }

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NetTrafficService.ACTION_SERVICE_RUN:
                    checkBoxPreference.setChecked(true);
                    break;
                case NetTrafficService.ACTION_SERVICE_DIED:
                    checkBoxPreference.setChecked(false);
            }
        }
    }
}
