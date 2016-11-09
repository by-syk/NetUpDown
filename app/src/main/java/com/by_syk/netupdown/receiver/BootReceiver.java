package com.by_syk.netupdown.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.by_syk.netupdown.service.NetTrafficService;

/**
 * Created by By_syk on 2016-11-09.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("run", false)) {
                    context.startService(new Intent(context, NetTrafficService.class));
                }
        }
    }
}
