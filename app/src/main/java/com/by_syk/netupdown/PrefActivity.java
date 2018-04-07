/**
 * Copyright 2016-2018 By_syk
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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.by_syk.lib.sp.SP;
import com.by_syk.netupdown.service.NetTrafficService;
import com.by_syk.netupdown.util.ExtraUtil;

/**
 * Created by By_syk on 2016-11-08.
 */

public class PrefActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private SP sp;

    private CheckBoxPreference checkBoxPreference;

    private ServiceReceiver serviceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        checkBoxPreference = (CheckBoxPreference) findPreference("run");
        checkBoxPreference.setOnPreferenceChangeListener(this);

        serviceReceiver = new ServiceReceiver();

        sp = new SP(this);
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
                    if (!sp.getBoolean("priorityHint")) {
                        setPriorityAndRun();
                    } else {
                        runService();
                    }
                } else {
                    stopService();
                }
                return false;
            default:
                return true;
        }
    }

    private void setPriorityAndRun() {
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_title_window_priority)
                .setMessage(R.string.priority_desc)
                .setPositiveButton(R.string.dlg_bt_high_priority, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sp.put("window", WindowManager.LayoutParams.TYPE_SYSTEM_ERROR)
                                .put("priorityHint", true).save();
                        runService();
                    }
                })
                .setNegativeButton(R.string.dlg_bt_low_priority, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sp.put("window", WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                                .put("priorityHint", true).save();
                        runService();
                    }
                })
                .create();
        dlg.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_faq:
                ExtraUtil.visitUrl(this, getString(R.string.faq_url));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
