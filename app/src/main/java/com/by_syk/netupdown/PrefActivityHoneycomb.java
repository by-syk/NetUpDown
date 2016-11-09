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
