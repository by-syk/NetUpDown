/**
 * Copyright 2016-2017 By_syk
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.by_syk.netupdown.service.NetTrafficService;
import com.by_syk.netupdown.util.C;

/**
 * Created by By_syk on 2016-11-09.
 */

public class QuickRunActivity extends Activity {
    private final static String ACTION_RUN = "com.by_syk.unicode.ACTION_RUN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tryRun();
    }

    @TargetApi(23)
    private void tryRun() {
        if (!ACTION_RUN.equals(getIntent().getAction())) {
            return;
        }

        if (C.SDK < 23 || Settings.canDrawOverlays(this)) {
            if (!NetTrafficService.isRunning) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putBoolean("run", true).commit();
                startService(new Intent(getApplicationContext(), NetTrafficService.class));
            }
        }

        finish();
    }
}
