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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.by_syk.netupdown.fragment.PrefFragment;
import com.by_syk.netupdown.util.C;

/**
 * Created by By_syk on 2016-11-08.
 */

public class PrefActivityHoneycomb extends Activity {
    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (C.SDK < 11) {
            startActivity(new Intent(this, PrefActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_pref);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PrefFragment prefFragment = new PrefFragment();
        fragmentTransaction.add(R.id.fragment, prefFragment);
        fragmentTransaction.commit();
    }
}
