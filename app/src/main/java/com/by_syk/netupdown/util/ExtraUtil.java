package com.by_syk.netupdown.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Locale;

/**
 * Created by By_syk on 2016-11-09.
 */

public class ExtraUtil {
    public static String getReadableNetSpeed(long netSpeed) {
        if (netSpeed < 1024) {
            return netSpeed + "B/s";
        } else if (netSpeed < 1024 * 1024) {
            return String.format(Locale.US, "%.1fKB/s", (netSpeed / 1024f));
        } else if (netSpeed < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.2fMB/s", (netSpeed / (1024f * 1024f)));
        } else {
            return String.format(Locale.US, "%.2fGB/s", (netSpeed / (1024f * 1024f * 1024f)));
        }
    }

    public static String getReadableBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1fKB", (bytes / 1024f));
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.2fMB", (bytes / (1024f * 1024f)));
        } else {
            return String.format(Locale.US, "%.2fGB", (bytes / (1024f * 1024f * 1024f)));
        }
    }

    public static int getStatusHeight(Context context) {
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            height = context.getResources().getDimensionPixelSize(height);
            Log.d(C.LOG_TAG, "getStatusHeight1 " + height);
            return height;
        } catch (Exception e) {
            e.printStackTrace();
        }

        int height = (int) context.getResources().getDisplayMetrics().density * 25;
        Log.d(C.LOG_TAG, "getStatusHeight2 " + height);
        return height;
    }

    public static boolean isNetworkConnected(Context context, boolean isWifiOnly) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }

        boolean is_connected = networkInfo.isAvailable();
        if (isWifiOnly) {
            is_connected &= networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return is_connected;
    }

    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected(context, false);
    }
}
