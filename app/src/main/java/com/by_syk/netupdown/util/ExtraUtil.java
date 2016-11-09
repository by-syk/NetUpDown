package com.by_syk.netupdown.util;

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
}
