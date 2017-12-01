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

import android.net.TrafficStats;
import android.os.SystemClock;

/**
 * Created by By_syk on 2016-10-20.
 */

public class NetTrafficSpider {
    private static NetTrafficSpider netTrafficSpider = null;

    private long startTotalBytes = 0;
    private long lastTotalBytes = TrafficStats.UNSUPPORTED; // UP+DOWN Mobile+WiFi
    private long lastTotalTxBytes = TrafficStats.UNSUPPORTED; // UP Mobile+WiFi
    private long lastTotalRxBytes = TrafficStats.UNSUPPORTED; // DOWN Mobile+WiFi

    private long usedBytes = 0;
    private int netSpeed = 0;
    private int netSpeedUp = 0;
    private int netSpeedDown = 0;

//    private int[] recentBytes = new int[16];
//    private int recentIndex = 0;

    private int refreshPeriod = 1000;

    private Thread thread = null;

    private boolean reqStop = false;

    private Callback callback = null;

    private NetTrafficSpider() {}

    public static NetTrafficSpider getInstance() {
        if (netTrafficSpider == null) {
            netTrafficSpider = new NetTrafficSpider();
        }
        return netTrafficSpider;
    }

    public void start() {
        if (callback != null) {
            callback.beforeStart();
        }

        resetUsedBytes();

        if (thread == null) {
            thread = new Thread(new MyThread());
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop() {
        thread = null;
        reqStop = true;
    }

    public void reset() {
        startTotalBytes = 0;
        lastTotalBytes = TrafficStats.UNSUPPORTED;
        lastTotalTxBytes = TrafficStats.UNSUPPORTED;
        lastTotalRxBytes = TrafficStats.UNSUPPORTED;
        refreshPeriod = 1000;
        reqStop = false;
    }

    public long getStartTotalBytes() {
        return startTotalBytes;
    }

    public void setStartTotalBytes(long startTotalBytes) {
        if (startTotalBytes < TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) {
            this.startTotalBytes = startTotalBytes;
        }
    }

    public void resetUsedBytes() {
        startTotalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
    }

    public void setRefreshPeriod(int refreshPeriod) {
        if (refreshPeriod > 0) {
            this.refreshPeriod = refreshPeriod;
        }
    }

    public int getRefreshPeriod() {
        return refreshPeriod;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public int getNetSpeed() {
        return netSpeed;
    }

    public int getNetSpeedUp() {
        return netSpeedUp;
    }

    public int getNetSpeedDown() {
        return netSpeedDown;
    }

    public String getReadableUsedBytes() {
        return ExtraUtil.getReadableBytes(usedBytes);
    }

    public String getReadableNetSpeed() {
        return ExtraUtil.getReadableNetSpeed(netSpeed);
    }

    public String getReadableNetSpeedUp() {
        return ExtraUtil.getReadableNetSpeed(netSpeedUp);
    }

    public String getReadableNetSpeedDown() {
        return ExtraUtil.getReadableNetSpeed(netSpeedDown);
    }

    private class MyThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (reqStop) {
                    if (callback != null) {
                        callback.afterStop();
                    }
                    return;
                }

                if (lastTotalTxBytes == TrafficStats.UNSUPPORTED) {
                    lastTotalTxBytes = TrafficStats.getTotalTxBytes();
                    lastTotalRxBytes = TrafficStats.getTotalRxBytes();
                    lastTotalBytes = lastTotalTxBytes + lastTotalRxBytes;
                    SystemClock.sleep(refreshPeriod);
                    continue;
                }

                long total = TrafficStats.getTotalTxBytes();
                netSpeedUp = (int) ((total - lastTotalTxBytes) * 1000 / refreshPeriod);
                lastTotalTxBytes = total;

                total = TrafficStats.getTotalRxBytes();
                netSpeedDown = (int) ((total - lastTotalRxBytes) * 1000 / refreshPeriod);
                lastTotalRxBytes = total;

                total = lastTotalTxBytes + lastTotalRxBytes;
                netSpeed = (int) ((total - lastTotalBytes) * 1000 / refreshPeriod);
//                recentBytes[(++recentIndex) % recentBytes.length] = (int) (total - lastTotalBytes);
                lastTotalBytes = total;

                usedBytes = lastTotalBytes - startTotalBytes;

                if (callback != null) {
                    callback.onUpdate(netSpeed, netSpeedUp, netSpeedDown, usedBytes,
                            getReadableNetSpeed(), getReadableNetSpeedUp(), getReadableNetSpeedDown(),
                            getReadableUsedBytes());
                }

                SystemClock.sleep(refreshPeriod);
            }
        }
    }

//    private float getStandardDeviation(int[] arr) {
//        float sum = 0; // byte
//        for (int a : arr) {
//            sum += a;
//        }
//        float average = sum / arr.length;
//        sum = 0; // kb
//        for (int a : arr) {
//            sum += (a - average) * (a - average) / (1024 * 1024);
//        }
//        average = (float) Math.sqrt(sum / arr.length);
//        return average;
//    }

    public interface Callback {
        void beforeStart();
        void onUpdate(long netSpeed, long netSpeedUp, long netSpeedDown, long usedBytes,
                      String readableNetSpeed, String readableNetSpeedUp, String readableNetSpeedDown,
                      String readableUsedBytes);
        void afterStop();
    }
}
