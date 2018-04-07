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

package com.by_syk.netupdown.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.by_syk.lib.sp.SP;
import com.by_syk.netupdown.R;
import com.by_syk.netupdown.util.C;
import com.by_syk.netupdown.util.ExtraUtil;
import com.by_syk.netupdown.util.NetTrafficSpider;
import com.by_syk.netupdown.widget.FloatTextView;

/**
 * Created by By_syk on 2016-11-08.
 */

public class NetTrafficService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private ViewGroup viewRoot;
    private FloatTextView tvSpeed;
    private TextView tvOptionSpeed;
    private TextView tvOptionFlow;
    private TextView tvOptionClose;

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

    private Runnable resumeViewRunnable = null;

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

        windowManager.removeView(viewRoot);

        unregisterReceiver(screenReceiver);

        sendBroadcast(new Intent(ACTION_SERVICE_DIED));

        super.onDestroy();
    }

    private void init() {
        sp = new SP(this);
        mode = sp.getInt("mode", MODE_SPEED);
        if (!sp.contains("y")) {
            sp.save("y", ExtraUtil.getStatusHeight(this));
        }
    }

    private void initView() {
        viewRoot = (ViewGroup) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.view_window, null);

        tvSpeed = viewRoot.findViewById(R.id.tv_speed);
        tvSpeed.setOnMoveListener(new FloatTextView.OnMoveListener() {
            @Override
            public void onMove(int x, int y) {
                layoutParams.x = x;
                layoutParams.y = y;
                windowManager.updateViewLayout(viewRoot, layoutParams);

                sp.put("x", x).put("y", y).save();
            }

            @Override
            public void onTap() {
                toggleOptions(true);
                touchFeedback();
            }

            @Override
            public void onDoubleTap() {
                switchMode();
                touchFeedback();
            }

            @Override
            public void onTripleTap() {
                tvOptionClose.performClick();
            }

            @Override
            public void onLongPress() {
                if (C.SDK >= 26) { // TODO SDK 26
                    return;
                }
                touchFeedback();
                switchWindow(layoutParams.type, true);
            }
        });

        tvOptionSpeed = viewRoot.findViewById(R.id.tv_option_speed);
        tvOptionSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOptions(false);
                switchMode(MODE_SPEED);
                touchFeedback();
            }
        });
        tvOptionFlow = viewRoot.findViewById(R.id.tv_option_flow);
        tvOptionFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOptions(false);
                switchMode(MODE_FLOW);
                touchFeedback();
            }
        });
        tvOptionClose = viewRoot.findViewById(R.id.tv_option_close);
        tvOptionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchFeedback();
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(NetTrafficService.this);
                sp.edit().putBoolean("run", false).commit();
                stopSelf();
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
        // TODO SDK 26
        windowManager.addView(viewRoot, layoutParams);
    }

    private void initThread() {
        netTrafficSpider.reset();
        netTrafficSpider.setRefreshPeriod(1500);
        netTrafficSpider.setCallback(new NetTrafficSpider.Callback() {
            private String text;
            private boolean flow_mode_tick = false;

            @Override
            public void beforeStart() {}

            @Override
            public void onUpdate(long netSpeed, long netSpeedUp, long netSpeedDown, long usedBytes) {
                if (isSleep) { // Do not update view when screen is off.
                    return;
                }
//                Log.d(C.LOG_TAG, "NetTrafficSpider.Callback onUpdate: "
//                        + ExtraUtil.getReadableNetSpeed(netSpeed));
                if (mode == MODE_SPEED) {
                    text = ExtraUtil.getReadableNetSpeed(netSpeed);
//                    text = "σ" + (int) variance + " " + ExtraUtil.getReadableNetSpeed(netSpeed);
//                    text = ExtraUtil.getReadableNetSpeed(netSpeedUp) + " | "
//                            + ExtraUtil.getReadableNetSpeed(netSpeedDown);
//                    text = "▲" + ExtraUtil.getReadableNetSpeed(netSpeedUp) + "\n▼"
//                            + ExtraUtil.getReadableNetSpeed(netSpeedDown);
                } else {
                    text = ExtraUtil.getReadableBytes(usedBytes);
                    text += (flow_mode_tick = !flow_mode_tick) ? "." : " ";
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

    private void toggleOptions(boolean toOpen) {
        if (toOpen) {
            tvOptionSpeed.setTextColor(mode == MODE_SPEED ? Color.GREEN : Color.WHITE);
            tvOptionFlow.setTextColor(mode == MODE_FLOW ? Color.GREEN : Color.WHITE);

            tvOptionSpeed.setVisibility(View.VISIBLE);
            tvOptionFlow.setVisibility(View.VISIBLE);
            tvOptionClose.setVisibility(View.VISIBLE);
            tvSpeed.setVisibility(View.GONE);

            if (resumeViewRunnable != null) {
                tvSpeed.removeCallbacks(resumeViewRunnable);
            }
            tvSpeed.postDelayed(resumeViewRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        toggleOptions(false);
                    }
                }
            }, 4000);
        } else {
            tvOptionSpeed.setVisibility(View.GONE);
            tvOptionFlow.setVisibility(View.GONE);
            tvOptionClose.setVisibility(View.GONE);
            tvSpeed.setVisibility(View.VISIBLE);
        }
    }

    
    private void switchMode(int targetMode) {
        if (targetMode == MODE_FLOW) {
            netTrafficSpider.resetUsedBytes();
            netTrafficSpider.setRefreshPeriod(2000);
            mode = MODE_FLOW;

            sp.put("startTotalBytes", netTrafficSpider.getStartTotalBytes());

            tvSpeed.setText(getString(R.string.mode_flow));
        } else {
            netTrafficSpider.setRefreshPeriod(1500);
            mode = MODE_SPEED;

            tvSpeed.setText(getString(R.string.mode_speed));
        }
        sp.put("mode", mode).save();
    }

    private void switchMode() {
        switchMode(mode == MODE_SPEED ? MODE_FLOW : MODE_SPEED);
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
            windowManager.removeView(viewRoot);
            windowManager.addView(viewRoot, layoutParams);
        }
    }

    private void touchFeedback() {
        tvSpeed.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
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
