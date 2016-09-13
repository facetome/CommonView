package com.basic.commonview.util;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.Calendar;

/**
 * 通用帮助类.
 */
public final class Utility {

    private Utility() {
        // make it as private
    }

    /**
     * 获取指定年月的开始时间和结束时间.
     *
     * @param year    指定的年
     * @param month   指定的月份
     * @param isStart 是否是开始时间
     * @return 时间戳(秒)
     */
    public static long getMonthStartOrEndInSecond(int year, int month, boolean isStart) {

        Calendar calendar = genDefaultCalender();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        if (isStart) {
            return calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
        } else {
            Calendar markCalendar = Calendar.getInstance();
            int currentYear = markCalendar.get(Calendar.YEAR);
            int currentMonth = markCalendar.get(Calendar.MONTH);
            if (year == currentYear && month == currentMonth + 1) {
                return markCalendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            } else {
                calendar.set(Calendar.MONTH, month);
                return (calendar.getTimeInMillis() - DateUtils.SECOND_IN_MILLIS) / DateUtils
                        .SECOND_IN_MILLIS;
            }
        }
    }

    /**
     * 获取指定年份的开始时间戳或者结束时间戳.
     *
     * @param year    年
     * @param isStart 是否是一年的开始
     * @return 时间戳（秒）
     */
    public static long getYearStartOrEnd(int year, boolean isStart) {
        Calendar calendar = genDefaultCalender();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 0);

        if (isStart) {
            return calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
        } else {
            Calendar markCalender = Calendar.getInstance();
            if (year == markCalender.get(Calendar.YEAR)) {
                return markCalender.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            } else {
                calendar.set(Calendar.YEAR, year + 1);
                return (calendar.getTimeInMillis() - DateUtils.SECOND_IN_MILLIS) / DateUtils
                        .SECOND_IN_MILLIS;
            }
        }
    }

    private static Calendar genDefaultCalender() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 获取屏幕宽度.
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
