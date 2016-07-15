
package com.basic.commonview.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * Log Utility for developing. Call these methods instead of using android.util.Log methods.
 * This is convenient for control log output by ourselves.
 */
public final class LogUtils {
    /**
     * The log tag of truck_manager client.
     */
    private static final String LOG_TAG = "truck_manager";

    private LogUtils() {
        // make it as private
    }

    /**
     * Log in debug level.
     *
     * @param subTag the sub log tag, could be empty
     * @param msg    log message
     */
    public static void d(String subTag, String msg) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, combineMessage(subTag, msg));
        }
    }

    /**
     * Log in error level.
     *
     * @param subTag the sub log tag, could be empty
     * @param msg    log message
     */
    public static void e(String subTag, String msg) {
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, combineMessage(subTag, msg));
        }
    }

    /**
     * Log in error level.
     *
     * @param subTag the sub log tag, could be empty
     * @param tr     the throwable
     */
    public static void e(String subTag, Throwable tr) {
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, combineMessage(subTag, tr.getMessage()), tr);
        }
    }

    /**
     * Log in error level with throwable.
     *
     * @param subTag the sub log tag, could be empty
     * @param msg    log message
     * @param tr     the throwable
     */
    public static void e(String subTag, String msg, Throwable tr) {
        if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
            Log.e(LOG_TAG, combineMessage(subTag, msg), tr);
        }
    }

    private static String combineMessage(String subTag, String msg) {
        if (!TextUtils.isEmpty(subTag)) {
            return subTag + " : " + msg;
        }
        return msg;
    }
}
