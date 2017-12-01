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

package com.by_syk.netupdown.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

/**
 * Created by By_syk on 2016-11-09.
 */

public class ExtraUtil {
    public static String getReadableNetSpeed(long netSpeed) {
        if (netSpeed == 0) {
            return "0.0B/s";
        } else if (netSpeed < 1024) {
            return netSpeed + "B/s";
        } else if (netSpeed < 1024 * 100) {
            return String.format(Locale.US, "%.1fK/s", (netSpeed / 1024f));
        } else if (netSpeed < 1024 * 1024) {
            return (netSpeed / 1024) + "K/s";
        } else if (netSpeed < 1024 * 1024 * 10) {
            return String.format(Locale.US, "%.2fM/s", (netSpeed / (1024f * 1024f)));
        } else if (netSpeed < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.1fM/s", (netSpeed / (1024f * 1024f)));
        } else {
            return String.format(Locale.US, "%.2fG/s", (netSpeed / (1024f * 1024f * 1024f)));
        }
    }

    public static String getReadableBytes(long bytes) {
        if (bytes == 0) {
            return "0.0B";
        } else if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + "KB";
        } else if (bytes < 1024 * 1024 * 100) {
            return String.format(Locale.US, "%.2fMB", (bytes / (1024f * 1024f)));
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.1fMB", (bytes / (1024f * 1024f)));
        } else {
            return String.format(Locale.US, "%.2fGB", (bytes / (1024f * 1024f * 1024f)));
        }
    }

    public static int getStatusHeight(Context context) {
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int id = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            return context.getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }

        return (int) context.getResources().getDisplayMetrics().density * 25;
    }

//    public static boolean isNetworkConnected(Context context, boolean isWifiOnly) {
//        if (context == null) {
//            return false;
//        }
//
//        ConnectivityManager connectivityManager = (ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivityManager == null) {
//            return false;
//        }
//
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        if (networkInfo == null) {
//            return false;
//        }
//
//        boolean is_connected = networkInfo.isAvailable();
//        if (isWifiOnly) {
//            is_connected &= networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
//        }
//
//        return is_connected;
//    }
//
//    public static boolean isNetworkConnected(Context context) {
//        return isNetworkConnected(context, false);
//    }

    public static void visitUrl(Context context, String url) {
        if (context == null || url == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
