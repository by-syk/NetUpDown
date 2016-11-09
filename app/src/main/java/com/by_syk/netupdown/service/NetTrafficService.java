package com.by_syk.netupdown.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.by_syk.netupdown.R;
import com.by_syk.netupdown.util.C;
import com.by_syk.netupdown.util.NetTrafficSpider;
import com.by_syk.netupdown.widget.FloatTextView;

/**
 * Created by By_syk on 2016-11-08.
 */

public class NetTrafficService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private FloatTextView tvSpeed;

    private NetTrafficSpider netTrafficSpider = NetTrafficSpider.getInstance();

    private ScreenReceiver screenReceiver;

    private boolean isSleep = false;

    private static final int MODE_SPEED = 0;
    private static final int MODE_FLOW = 1;
    private static int mode = MODE_SPEED;

    private static int x = 0;
    private static int y = -1;

    public static boolean isRunning = false;

    public static final String ACTION_SERVICE_RUN = "com.by_syk.netupdown.ACTION_SERVICE_RUN";
    public static final String ACTION_SERVICE_DIED = "com.by_syk.netupdown.ACTION_SERVICE_DIED";

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;

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
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRunning = false;

        netTrafficSpider.stop();

        windowManager.removeView(tvSpeed);

        x = layoutParams.x;
        y = layoutParams.y;

        unregisterReceiver(screenReceiver);

        sendBroadcast(new Intent(ACTION_SERVICE_DIED));
    }

    private void initView() {
        windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.x = x;
        if (y < 0) {
            y = (int) (getResources().getDisplayMetrics().density * 25);
        }
        layoutParams.y = y;

        tvSpeed = (FloatTextView) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.view_window, null);
        tvSpeed.setOnMoveListener(new FloatTextView.OnMoveListener() {
            @Override
            public void onMove(int x, int y) {
                layoutParams.x = x;
                layoutParams.y = y;
                windowManager.updateViewLayout(tvSpeed, layoutParams);
            }

            @Override
            public void onDoubleTap() {
                if (mode == MODE_SPEED) {
                    netTrafficSpider.resetUsedBytes();
                    netTrafficSpider.setRefreshPeriod(2000);
                    mode = MODE_FLOW;
                } else {
                    netTrafficSpider.setRefreshPeriod(1500);
                    mode = MODE_SPEED;
                }
            }

            @Override
            public void onTripleTap() {
                stopSelf();
            }
        });

        windowManager.addView(tvSpeed, layoutParams);
    }

    private void initThread() {
        netTrafficSpider.reset();
        netTrafficSpider.setRefreshPeriod(1500);
        netTrafficSpider.setCallback(new NetTrafficSpider.Callback() {
            @Override
            public void beforeStart() {}

            @Override
            public void onUpdate(long netSpeed, long netSpeedUp, long netSpeedDown, long usedBytes,
                                 String readableNetSpeed, String readableNetSpeedUp, String readableNetSpeedDown, String readableUsedBytes) {
                if (isSleep) {
                    return;
                }
                Log.d(C.LOG_TAG, "NetTrafficSpider.Callback onUpdate: " + readableNetSpeed);
                final String TEXT;
                if (mode == MODE_SPEED) {
                    TEXT = readableNetSpeed;
                } else {
                    TEXT = readableUsedBytes;
                }
                tvSpeed.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSpeed.setText(TEXT);
                    }
                });
            }

            @Override
            public void afterStop() {}
        });
        netTrafficSpider.start();
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
