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

package com.by_syk.netupdown.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.by_syk.lib.storage.SP;
import com.by_syk.netupdown.R;
import com.by_syk.netupdown.util.ExtraUtil;
import com.by_syk.netupdown.util.NetTrafficSpider;
import com.by_syk.netupdown.widget.FloatTextView;

/**
 * Created by By_syk on 2016-11-08.
 */

public class NetTrafficService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private FloatTextView tvSpeed;

    private SP sp;

    private NetTrafficSpider netTrafficSpider = NetTrafficSpider.getInstance();

    private ScreenReceiver screenReceiver;

    private boolean isSleep = false;

    private static final int MODE_SPEED = 0;
    private static final int MODE_FLOW = 1;
    private int mode = MODE_SPEED;

    public static boolean isRunning = false;

    public static final String ACTION_SERVICE_RUN = "com.by_syk.netupdown.ACTION_SERVICE_RUN";
    public static final String ACTION_SERVICE_DIED = "com.by_syk.netupdown.ACTION_SERVICE_DIED";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;

        init();

        initView();

        initThread();

        screenReceiver = new ScreenReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, intentFilter);

        sendBroadcast(new Intent(ACTION_SERVICE_RUN));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (mode == MODE_FLOW) {
                sp.save("startTotalBytes", netTrafficSpider.getStartTotalBytes());
            }
        } else { // Auto restart
            if (mode == MODE_FLOW) {
                netTrafficSpider.setStartTotalBytes(sp.getLong("startTotalBytes", Long.MAX_VALUE));
            }
        }

        // If the service's process is killed while it is started,
        // later the system will try to re-create the service.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;

        netTrafficSpider.stop();

        windowManager.removeView(tvSpeed);

        unregisterReceiver(screenReceiver);

        sendBroadcast(new Intent(ACTION_SERVICE_DIED));

        super.onDestroy();
    }

    private void init() {
        sp = new SP(this, false);
        mode = sp.getInt("mode", MODE_SPEED);
        if (!sp.contains("y")) {
            sp.save("y", ExtraUtil.getStatusHeight(this));
        }
    }

    private void initView() {
        tvSpeed = (FloatTextView) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.view_window, null);
        tvSpeed.setOnMoveListener(new FloatTextView.OnMoveListener() {
            @Override
            public void onMove(int x, int y) {
                layoutParams.x = x;
                layoutParams.y = y;
                windowManager.updateViewLayout(tvSpeed, layoutParams);

                sp.put("x", x).put("y", y).save();
            }

            @Override
            public void onDoubleTap() {
                switchMode();
                touchFeedback();
            }

            @Override
            public void onTripleTap() {
                touchFeedback();
                stopSelf();
            }

            @Override
            public void onLongPress() {
                touchFeedback();
                switchWindow(layoutParams.type, true);
            }
        });

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.x = sp.getInt("x");
        layoutParams.y = sp.getInt("y");

        switchWindow(sp.getInt("window", WindowManager.LayoutParams.TYPE_SYSTEM_ERROR), false);

        windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        windowManager.addView(tvSpeed, layoutParams);
    }

    private void initThread() {
        netTrafficSpider.reset();
        netTrafficSpider.setRefreshPeriod(1500);
        netTrafficSpider.setCallback(new NetTrafficSpider.Callback() {
            private String text;

            @Override
            public void beforeStart() {}

            @Override
            public void onUpdate(long netSpeed, long netSpeedUp, long netSpeedDown, long usedBytes,
                                 String readableNetSpeed, String readableNetSpeedUp, String readableNetSpeedDown, String readableUsedBytes) {
                if (isSleep) { // Do not update view when screen is off.
                    return;
                }
//                Log.d(C.LOG_TAG, "NetTrafficSpider.Callback onUpdate: " + readableNetSpeed);
                if (mode == MODE_SPEED) {
                    text = readableNetSpeed;
//                    text = "σ" + (int) variance + " " + readableNetSpeed;
//                    text = readableNetSpeedUp + " | " + readableNetSpeedDown;
                } else {
                    text = readableUsedBytes;
                }
                tvSpeed.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSpeed.setText(text);
                    }
                });
            }

            @Override
            public void afterStop() {}
        });
        netTrafficSpider.start();
    }

    private void switchMode() {
        if (mode == MODE_SPEED) {
            netTrafficSpider.resetUsedBytes();
            netTrafficSpider.setRefreshPeriod(2000);
            mode = MODE_FLOW;

            sp.put("startTotalBytes", netTrafficSpider.getStartTotalBytes());
        } else {
            netTrafficSpider.setRefreshPeriod(1500);
            mode = MODE_SPEED;
        }
        sp.put("mode", mode).save();

        tvSpeed.setText(getString(R.string.app_name));
    }

    private void switchWindow(int curWindow, boolean execute) {
        if (execute) {
            // TYPE_SYSTEM_ALERT / TYPE_TOAST
            curWindow = curWindow == WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
                    ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }

        if (curWindow == WindowManager.LayoutParams.TYPE_SYSTEM_ALERT) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            tvSpeed.setOffsetY(ExtraUtil.getStatusHeight(this));
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            tvSpeed.setOffsetY(0);
        }

        sp.save("window", curWindow);

        if (execute) {
//            windowManager.updateViewLayout(tvSpeed, layoutParams);
            windowManager.removeView(tvSpeed);
            windowManager.addView(tvSpeed, layoutParams);
        }
    }

    private void touchFeedback() {
        tvSpeed.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    isSleep = true;
                    break;
                case Intent.ACTION_SCREEN_ON:
                    isSleep = false;
            }
        }
    }
}
