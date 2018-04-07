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

package com.by_syk.netupdown.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.WindowManager;

import com.by_syk.lib.sp.SP;
import com.by_syk.netupdown.R;
import com.by_syk.netupdown.service.NetTrafficService;
import com.by_syk.netupdown.util.C;

/**
 * Created by By_syk on 2016-11-08.
 */

@TargetApi(11)
public class PrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private SP sp;

    private SwitchPreference switchPreference;
    private CheckBoxPreference checkBoxPreference;

    private ServiceReceiver serviceReceiver;

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public static PrefFragment newInstance() {
        return new PrefFragment();
    }

    @TargetApi(14)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*
         * SwitchPreferences calls multiple times the onPreferenceChange() method
         * It is due to the bug in SwitchPreference implementation.
         * And it's solved in API 21+
         */
        //if (C.SDK >= 14) {
        if (C.SDK >= 21) {
            switchPreference = (SwitchPreference) findPreference("run");
        } else {
            checkBoxPreference = (CheckBoxPreference) findPreference("run");
        }
        findPreference("run").setOnPreferenceChangeListener(this);

        serviceReceiver = new ServiceReceiver();

        sp = new SP(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isChecked() && !NetTrafficService.isRunning) {
            if (canDrawOverlays()) {
                runService();
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetTrafficService.ACTION_SERVICE_RUN);
        intentFilter.addAction(NetTrafficService.ACTION_SERVICE_DIED);
        getActivity().registerReceiver(serviceReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(serviceReceiver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "run":
                boolean isChecked = (Boolean) newValue;
                if (isChecked) {
                    if (!NetTrafficService.isRunning) {
                        if (canDrawOverlays()) {
                            if (!sp.getBoolean("priorityHint")) {
                                setPriorityAndRun();
                            } else {
                                runService();
                            }
                        } else { // No permission
                            requestDrawOverLays();
                        }
                    }
                } else {
                    stopService();
                }
                return false;
            default:
                return true;
        }
    }

    @TargetApi(14)
    private void setChecked(boolean checked) {
        if (C.SDK >= 21) {
            switchPreference.setChecked(checked);
        } else {
            checkBoxPreference.setChecked(checked);
        }
    }

    @TargetApi(14)
    private boolean isChecked() {
        if (C.SDK >= 21) {
            return switchPreference.isChecked();
        } else {
            return checkBoxPreference.isChecked();
        }
    }

    private void runService() {
        getActivity().startService(new Intent(getActivity().getApplicationContext(), NetTrafficService.class));
    }

    private void stopService() {
        getActivity().stopService(new Intent(getActivity().getApplicationContext(), NetTrafficService.class));
    }

    @TargetApi(23)
    private boolean canDrawOverlays() {
        return C.SDK < 23 || Settings.canDrawOverlays(getActivity());
    }

    @TargetApi(23)
    private void requestDrawOverLays() {
        if (canDrawOverlays()) {
            return;
        }

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }

    private void setPriorityAndRun() {
        if (C.SDK >= 26) { // TODO SDK 26
            sp.put("window", WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                    .put("priorityHint", true).save();
            runService();
            return;
        }

        AlertDialog dlg = new AlertDialog.Builder(getActivity())
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

    @TargetApi(23)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (canDrawOverlays()) {
                setChecked(true);
            }
        }
    }

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            switch (intent.getAction()) {
                case NetTrafficService.ACTION_SERVICE_RUN:
                    setChecked(true);
                    break;
                case NetTrafficService.ACTION_SERVICE_DIED:
                    if (isChecked()) {
                        setChecked(false);
                    }
            }
        }
    }
}
