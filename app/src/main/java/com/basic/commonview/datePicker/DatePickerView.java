package com.basic.commonview.datePicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.basic.commonview.R;
import com.basic.commonview.datePicker.WheelView.CurrentItemListener;
import com.basic.commonview.datePicker.adapter.AbstractWheelTextAdapter;
import com.basic.commonview.datePicker.listener.OnWheelChangedListener;
import com.basic.commonview.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 年月日时分秒选择器,可选择时间样式.
 */
public class DatePickerView extends LinearLayout implements CurrentItemListener {

    /**
     * 年月日时样式.
     */
    public static final int STYLE_TYPE_YEAR_MONTH_DAY_HOUR = 1;
    /**
     * 年月日样式.
     */
    public static final int STYLE_TYPE_YEAR_MONTH_DAY = 2;
    /**
     * 时分秒样式 TODO 未测试所以暂时不开放.
     */
    private static final int STYLE_TYPE_HOUR_MINUTE_SECOND = 4;

    /**
     * 时分样式.
     */
    public static final int STYLE_TYPE_HOUR_MINUTE = 3;

    public static final String WHEEL_YEAR = "year";
    public static final String WHEEL_MONTH = "month";
    public static final String WHEEL_DAY = "day";
    public static final String WHEEL_HOUR_MINUTE = "hour_minute";
    public static final String WHEEL_HOUR = "hour";
    public static final String WHEEL_MINUTE = "minute";
    public static final String WHEEL_SECOND = "second";
    private Map<String, WheelView> mWheelViewMap = new HashMap<String, WheelView>();
    private static final double SHADOW_HEIGHT = 1.5;
    /**
     * Top and bottom shadows colors.
     */
    private static final int[] SHADOWS_COLORS = new int[]{0xFF111111, 0x00AAAAAA, 0x00AAAAAA};
    // Shadows drawables
    private GradientDrawable mTopShadow;
    private GradientDrawable mBottomShadow;
    private static final int WHEEL_DEFAULT_ITEMS = 5;
    private static final int DEFAULT_TEXT_SIZE = 20;
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#FF101010");
    private static final int DEFAULT_CENTER_TEXT_SIZE = 22;
    private static final int DEFAULT_CENTER_TEXT_COLOR = Color.BLUE;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float mAxisTextSize = DEFAULT_CENTER_TEXT_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mAxisTextColor = DEFAULT_CENTER_TEXT_COLOR;
    private static final int MAX_HOUR = 24;
    private static final int MAX_MINUTE = 60;
    private boolean mShouldShowShade = false;
    private YearWheelAdapter mYearWheelAdapter;
    private CustomizationMaxItemWheelAdapter mMonthWheelAdapter;
    private CustomizationMaxItemWheelAdapter mDayWheelAdapter;
    private CustomizationMaxItemWheelAdapter mHourAndMinuteWheelAdapter;
    private CustomizationMaxItemWheelAdapter mHourWheelAdapter;
    private CustomizationMaxItemWheelAdapter mMinuteWheelAdapter;
    private SecondWheelAdapter mSecondWheelAdapter;
    private Calendar mCalendar = Calendar.getInstance();
    private Calendar mMaxTimeCalendar = Calendar.getInstance();
    private Calendar mMinTimeCalender = Calendar.getInstance();
    private static final int DEFAULT_START_YEAR = 1970;
    private static final int NUMBER_TEN = 10;
    private int mStartYear;
    private int mEndYear;
    private long mMaxTime;
    private long mMinTime;
    private int mStyleType;

    /**
     * Constructor.
     *
     * @param context {@link Context}
     */
    public DatePickerView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor.
     *
     * @param context {@link Context}
     * @param attrs   {@link AttributeSet}
     */
    public DatePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor.
     *
     * @param context  {@link Context}
     * @param attrs    {@link AttributeSet}
     * @param defStyle view Style.
     */
    public DatePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // user can use self attrs to set the textSize and textColor in xml file.
        TypedArray pickerTypeArray = context.obtainStyledAttributes(attrs, R.styleable.wheelViewPicker);
        mTextSize = pickerTypeArray.getDimension(R.styleable.wheelViewPicker_textSize,
                DatePickerView.DEFAULT_TEXT_SIZE);
        mAxisTextSize = pickerTypeArray.getDimension(R.styleable
                .wheelViewPicker_axisTextSize, DEFAULT_CENTER_TEXT_SIZE);
        mTextColor = pickerTypeArray.getColor(R.styleable.wheelViewPicker_textColor,
                DEFAULT_TEXT_COLOR);
        mAxisTextColor = pickerTypeArray.getColor(R.styleable.wheelViewPicker_axisTextColor,
                DEFAULT_CENTER_TEXT_COLOR);
        pickerTypeArray.recycle();
    }

    private void initDefaultWheel(long second, int styleType) {
        mTextColor = getResources().getColor(R.color.C5);
        mAxisTextColor = getResources().getColor(R.color.C1);
        setOrientation(HORIZONTAL);
        mStyleType = styleType;
        // max time can be handled setting, but max date just less than or equal to current date.
        // if user set the max time greater than current time , the max time will be
        // replaced by current time forcedly.
        long maxTime = System.currentTimeMillis();
        if (mMaxTime == 0L) {
            mMaxTime = maxTime;
        }
        // 设置最小时间缺省
        if (mMinTime == 0L || mMinTime >= maxTime) {
            mMinTime = Utility.getYearStartOrEnd(DEFAULT_START_YEAR, true) * DateUtils.SECOND_IN_MILLIS;
        }
        mMaxTimeCalendar.setTimeInMillis(mMaxTime);
        mMinTimeCalender.setTimeInMillis(mMinTime);
        mCalendar.setTimeInMillis(second * DateUtils.SECOND_IN_MILLIS);
        mStartYear = mMinTimeCalender.get(Calendar.YEAR);
        mEndYear = mMaxTimeCalendar.get(Calendar.YEAR);
        removeAllViews();
        setGravity(Gravity.CENTER_VERTICAL);
        switch (mStyleType) {
            case STYLE_TYPE_YEAR_MONTH_DAY_HOUR:
                initYear(mStartYear, mEndYear);
                initMonth();
                initDay();
                initHourAndMinute();
                if (mYearWheelAdapter != null) {
                    int yearPosition = mYearWheelAdapter.getYearPosition(mCalendar.get(Calendar.YEAR));
                    onYearChanged(yearPosition);
                }
                break;
            case STYLE_TYPE_YEAR_MONTH_DAY:
                initYear(mStartYear, mEndYear);
                initMonth();
                initDay();
                if (mYearWheelAdapter != null) {
                    int yearPosition = mYearWheelAdapter.getYearPosition(mCalendar.get(Calendar.YEAR));
                    onYearChanged(yearPosition);
                }
                break;
            case STYLE_TYPE_HOUR_MINUTE_SECOND:
                initHour();
                initMinute();
                initSecond();
                break;
            case STYLE_TYPE_HOUR_MINUTE:
            default:
                initHour();
                initMinute();
                if (mHourWheelAdapter != null) {
                    int hourPosition = mCalendar.get(Calendar.HOUR_OF_DAY);
                    onHourChanged(hourPosition);
                }
                break;
        }

    }

    private void initYear(int startYear, int endYear) {
        if (mYearWheelAdapter == null) {
            mYearWheelAdapter = new YearWheelAdapter(getContext(), startYear, endYear, mTextSize, mTextColor);
        }
        int position = mYearWheelAdapter.getYearPosition(mCalendar.get(Calendar.YEAR));
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mYearWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCurrentItem(position);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                onYearChanged(newValue);
            }
        });
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_YEAR, wheel);
    }

    private void initMonth() {
        if (mMonthWheelAdapter == null) {
            String[] itemArray = getResources().getStringArray(R.array.month_picker);
            mMonthWheelAdapter = new CustomizationMaxItemWheelAdapter(getContext(), itemArray,
                    mTextSize, mTextColor);
        }
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mMonthWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCyclic(false);
        wheel.setCurrentItem(mCalendar.get(Calendar.MONTH) - getMinMonthOfYear(mCalendar.get(Calendar.YEAR)) + 1);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                onMonthChanged(newValue + mMonthWheelAdapter.getUnShowCount());
            }
        });
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_MONTH, wheel);
    }

    private void initDay() {
        int maxDay = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (mDayWheelAdapter == null) {
            String[] itemArray = getResources().getStringArray(R.array.day_picker);
            mDayWheelAdapter = new CustomizationMaxItemWheelAdapter(getContext(), itemArray,
                    mTextSize, mTextColor);
        }
        int minDay = getMinDayOfMonth(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH));
        mDayWheelAdapter.updateViewItem(maxDay, minDay - 1);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mDayWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCyclic(false);
        wheel.setCurrentItem(mCalendar.get(Calendar.DAY_OF_MONTH) - getMinDayOfMonth(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH)));
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                onDayChanged(newValue + mDayWheelAdapter.getUnShowCount());
            }
        });
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_DAY, wheel);
    }

    private void initHourAndMinute() {
        if (mHourAndMinuteWheelAdapter == null) {
            String[] itemArray = getResources().getStringArray(R.array.twenty_four_hour_minute_picker);
            mHourAndMinuteWheelAdapter = new CustomizationMaxItemWheelAdapter(getContext(),
                    itemArray, mTextSize, mTextColor);
        }
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mHourAndMinuteWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.HOUR_OF_DAY, newValue + mHourAndMinuteWheelAdapter.getUnShowCount());
            }
        });
        wheel.setCyclic(false);
        wheel.setCurrentItem(mCalendar.get(Calendar.HOUR_OF_DAY) - getMinHourOfDay(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)));
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_HOUR_MINUTE, wheel);
    }

    private void initHour() {
        if (mHourWheelAdapter == null) {
            String[] hourArray = new String[MAX_HOUR];
            for (int i = 0; i < MAX_HOUR; i++) {
                if (i < NUMBER_TEN) {
                    hourArray[i] = "0" + i;
                } else {
                    hourArray[i] = String.valueOf(i);
                }
            }
            mHourWheelAdapter = new CustomizationMaxItemWheelAdapter(getContext(), hourArray, mTextSize, mTextColor);
        }
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mHourWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                onHourChanged(newValue + mHourWheelAdapter.getUnShowCount());
            }
        });
        wheel.setCyclic(false);
        //小时是自身作为第一个联动，所以他需要自己进入的时候就进行最大时间和最小时间的确定.
        int maxHour = getMaxHourOfDay(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));
        int minHour = getMinHourOfDay(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));
        mHourWheelAdapter.updateViewItem(maxHour + 1, minHour);
        wheel.setCurrentItem(mCalendar.get(Calendar.HOUR_OF_DAY) - getMinHourOfDay(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)));
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_HOUR, wheel);
    }

    private void initMinute() {
        if (mMinuteWheelAdapter == null) {
            String[] minuteArray = new String[MAX_MINUTE];
            for (int i = 0; i < MAX_MINUTE; i++) {
                if (i < NUMBER_TEN) {
                    minuteArray[i] = "0" + i;
                } else {
                    minuteArray[i] = String.valueOf(i);
                }
            }
            mMinuteWheelAdapter = new CustomizationMaxItemWheelAdapter(getContext(), minuteArray, mTextSize, mTextColor);
        }
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mMinuteWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.MINUTE, newValue + mMinuteWheelAdapter.getUnShowCount());
            }
        });
        wheel.setCyclic(false);
        wheel.setCurrentItem(mCalendar.get(Calendar.MINUTE) - getMinMinuteOfHour(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), mCalendar.get(Calendar.HOUR_OF_DAY)));
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_MINUTE, wheel);
    }

    // TODO 暂时不考虑秒的时间选择器.
    private void initSecond() {
        if (mSecondWheelAdapter == null) {
            mSecondWheelAdapter = new SecondWheelAdapter(getContext(), mTextSize, mTextColor);
        }
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        WheelView wheel = new WheelView(getContext());
        wheel.setViewAdapter(mSecondWheelAdapter);
        wheel.setVisibleItems(WHEEL_DEFAULT_ITEMS);
        wheel.setCurrentItemListener(this);
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.SECOND, newValue);
            }
        });
        wheel.setCyclic(true);
        wheel.setCurrentItem(mCalendar.get(Calendar.SECOND));
        addView(wheel, layoutParams);
        mWheelViewMap.put(WHEEL_SECOND, wheel);
    }

    private void onYearChanged(int yearPosition) {
        WheelView monthWheel = mWheelViewMap.get(WHEEL_MONTH);
        int year = mYearWheelAdapter.getYear(yearPosition);
        mCalendar.set(Calendar.YEAR, year);
        if (monthWheel != null) {
            int maxMonth = getMaxMonthOfYear(year);
            int minMonth = getMinMonthOfYear(mCalendar.get(Calendar.YEAR));
            mMonthWheelAdapter.updateViewItem(maxMonth, minMonth - 1);
            int monthPosition = monthWheel.getCurrentItem() + mMonthWheelAdapter.getUnShowCount();
            if (mMonthWheelAdapter.getItemsCount() - 1 <= monthPosition) {
                monthPosition = mMonthWheelAdapter.getItemsCount() - 1;
                monthWheel.setCurrentItem(monthPosition);
                monthPosition += mMonthWheelAdapter.getUnShowCount();
                mCalendar.set(Calendar.MONTH, monthPosition);
            }
            onMonthChanged(monthPosition);
        }
    }

    /**
     * 设置当前最大日期值.
     * 默认会当前时间.
     * 单位为毫秒
     *
     * @param maxTime 最大日期.
     */
    public void setMaxTime(long maxTime) {
        mMaxTime = maxTime;
    }

    /**
     * 设置最小可选时间.
     * 默认为1970-01-01:00.00
     * 单位为毫秒
     *
     * @param minTime 最小可选时间
     */
    public void setMinTime(long minTime) {
        mMinTime = minTime;
    }

    private void onMonthChanged(int monthPosition) {
        WheelView dayWheel = mWheelViewMap.get(WHEEL_DAY);
        // 在设置月份时，天数不变，而刚好设置的月份么有次天，则月份会自动+1，比如，6月31号自动跳转为7.1号
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mCalendar.set(Calendar.MONTH, monthPosition);
        if (dayWheel != null) {
            int maxDay = getMaxDayOfMonth(mCalendar.get(Calendar.YEAR), monthPosition);
            int minDay = getMinDayOfMonth(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH));
            mDayWheelAdapter.updateViewItem(maxDay, minDay - 1);
            int dayPosition = dayWheel.getCurrentItem() + mDayWheelAdapter.getUnShowCount();
            if (mDayWheelAdapter.getItemsCount() - 1 <= dayPosition) {
                dayPosition = mDayWheelAdapter.getItemsCount() - 1;
                dayWheel.setCurrentItem(dayPosition);
                dayPosition += mDayWheelAdapter.getUnShowCount();
                mCalendar.set(Calendar.DAY_OF_MONTH, dayPosition + 1);
            }
            onDayChanged(dayPosition);
        }
    }

    private void onDayChanged(int dayPosition) {
        WheelView hourAndMinuteWheel = mWheelViewMap.get(WHEEL_HOUR_MINUTE);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayPosition + 1);
        if (hourAndMinuteWheel != null) {
            int maxHour = getMaxHourOfDay(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH));
            int minHour = getMinHourOfDay(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH));
            mHourAndMinuteWheelAdapter.updateViewItem(maxHour + 1, minHour);
            int hourPosition = hourAndMinuteWheel.getCurrentItem() + mHourAndMinuteWheelAdapter.getUnShowCount();
            if (mHourAndMinuteWheelAdapter.getItemsCount() - 1 <= hourPosition) {
                hourPosition = mHourAndMinuteWheelAdapter.getItemsCount() - 1;
                hourAndMinuteWheel.setCurrentItem(hourPosition);
                hourPosition += mHourAndMinuteWheelAdapter.getUnShowCount();
                mCalendar.set(Calendar.HOUR_OF_DAY, hourPosition);
            }
            mCalendar.set(Calendar.HOUR_OF_DAY, hourPosition);
        }
    }

    //采用 HH:mm模式下小时发生变化，带动分发生改变
    private void onHourChanged(int hourPosition) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourPosition);
        WheelView minuteView = mWheelViewMap.get(WHEEL_MINUTE);
        if (minuteView != null) {
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int maxMinute = getMaxMinuteOfHour(year, month, day, hour);
            int minMinute = getMinMinuteOfHour(year, month, day, hour);
            mMinuteWheelAdapter.updateViewItem(maxMinute + 1, minMinute);
            int minutePosition = minuteView.getCurrentItem() + mMinuteWheelAdapter.getUnShowCount();
            if (minutePosition >= mMinuteWheelAdapter.getItemsCount() - 1) {
                minutePosition = mMinuteWheelAdapter.getItemsCount() - 1;
                minuteView.setCurrentItem(minutePosition);
                minutePosition += mMinuteWheelAdapter.getUnShowCount();
            }
            mCalendar.set(Calendar.MINUTE, minutePosition);
        }
    }

    //minute从0开始
    private int getMaxMinuteOfHour(int year, int month, int day, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        int maxYear = mMaxTimeCalendar.get(Calendar.YEAR);
        int maxMonth = mMaxTimeCalendar.get(Calendar.MONTH);
        int maxDay = mMaxTimeCalendar.get(Calendar.DAY_OF_MONTH);
        int maxHour = mMaxTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.getActualMaximum(Calendar.MINUTE);
        if (maxYear == year && month == maxMonth && day == maxDay && maxHour == hour) {
            minute = mMaxTimeCalendar.get(Calendar.MINUTE);
        }
        return minute;
    }

    /**
     * 获取到指定年的最小月,第一个月应该是1.
     *
     * @param year 指定年.
     * @return 月份
     */
    private int getMinMonthOfYear(int year) {
        int minYear = mMinTimeCalender.get(Calendar.YEAR);
        int month = 0;
        if (minYear >= year) {
            month = mMinTimeCalender.get(Calendar.MONTH);
        }
        return month + 1;
    }

    // 最小的一天应该是1.
    private int getMinDayOfMonth(int year, int month) {
        int minYear = mMinTimeCalender.get(Calendar.YEAR);
        int minMonth = mMinTimeCalender.get(Calendar.MONTH);
        int minDay = 1;
        if (minYear >= year && minMonth >= month) {
            minDay = mMinTimeCalender.get(Calendar.DAY_OF_MONTH);
        }
        //Day是第一天开始的,不用+1
        return minDay;
    }

    //小时时间是从0开始，0.00-23.59
    private int getMinHourOfDay(int year, int month, int day) {
        int minYear = mMinTimeCalender.get(Calendar.YEAR);
        int minMonth = mMinTimeCalender.get(Calendar.MONTH);
        int minDay = mMinTimeCalender.get(Calendar.DAY_OF_MONTH);
        int minHour = 0;
        if (minYear >= year && minMonth >= month && minDay >= day) {
            minHour = mMinTimeCalender.get(Calendar.HOUR_OF_DAY);
        }
        return minHour;
    }

    //最小值为0
    private int getMaxHourOfDay(int year, int month, int day) {
        int maxHour = mMaxTimeCalendar.getActualMaximum(Calendar.HOUR_OF_DAY);
        if (year == mMaxTimeCalendar.get(Calendar.YEAR) && month == mMaxTimeCalendar.get(Calendar.MONTH)
                && day == mMaxTimeCalendar.get(Calendar.DAY_OF_MONTH)) {
            maxHour = mMaxTimeCalendar.get(Calendar.HOUR_OF_DAY);
        }
        //小时计算是从0开始的
        return maxHour;
    }

    private int getMinMinuteOfHour(int year, int month, int day, int hour) {
        int minYear = mMinTimeCalender.get(Calendar.YEAR);
        int minMonth = mMinTimeCalender.get(Calendar.MONTH);
        int minDay = mMinTimeCalender.get(Calendar.DAY_OF_MONTH);
        int minHour = mMinTimeCalender.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        if (minYear >= year && minMonth >= month && minDay >= day && minHour >= hour) {
            minute = mMinTimeCalender.get(Calendar.MINUTE);
        }
        //分钟从0开始.
        return minute;
    }

    private int getMaxMonthOfYear(int year) {
        int maxMonth = mMaxTimeCalendar.getActualMaximum(Calendar.MONTH);
        if (year == mMaxTimeCalendar.get(Calendar.YEAR)) {
            maxMonth = mMaxTimeCalendar.get(Calendar.MONTH);
        }
        //月份计算是从0开始的,取得当前年的月份+1为月份最大数量
        return maxMonth + 1;
    }

    private int getMaxDayOfMonth(int year, int month) {
        Calendar flagCalendar = Calendar.getInstance();
        flagCalendar.set(Calendar.YEAR, year);
        flagCalendar.set(Calendar.MONTH, month);
        flagCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = flagCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (year == mMaxTimeCalendar.get(Calendar.YEAR) && month == mMaxTimeCalendar.get(Calendar.MONTH)) {
            maxDay = mMaxTimeCalendar.get(Calendar.DAY_OF_MONTH);
        }
        //Day是第一天开始的,不用+1
        return maxDay;
    }

    /**
     * 获得当前时间(秒).
     *
     * @return 当前时间(秒)
     */
    public long getTimeInSecond() {
        return mCalendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
    }

    /**
     * 获得当前时间(毫秒).
     *
     * @return 当前时间(毫秒)
     */
    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    /**
     * 初始化时间选择器.
     *
     * @param second    当前时间(秒)
     * @param styleType 值为{@link #STYLE_TYPE_YEAR_MONTH_DAY_HOUR},{@link #STYLE_TYPE_YEAR_MONTH_DAY}
     *                  {@link #STYLE_TYPE_HOUR_MINUTE}
     */
    public void initDataPicker(long second, int styleType) {
        initDefaultWheel(second, styleType);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShouldShowShade) {
            // draw shadows.
            drawShadows(canvas);
        }
    }

    /**
     * 是否需要显示阴影.
     *
     * @param showShade true表示需要,默认不需要
     */
    public void setShouldShowShade(boolean showShade) {
        mShouldShowShade = showShade;
    }

    /**
     * Draws shadows on top and bottom of control.
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        if (mTopShadow == null) {
            mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }
        if (mBottomShadow == null) {
            mBottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }
        int height = (int) (SHADOW_HEIGHT * getHeight());
        mTopShadow.setBounds(0, 0, getWidth(), height);
        mTopShadow.draw(canvas);

        mBottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
        mBottomShadow.draw(canvas);
    }

    @Override
    public void onCurrentItemViewPosition(int position, View currentItem, LinearLayout parent) {
        drawItemColor(position, parent);
    }

    @Override
    public void onCurrentItem(int position) {
        //do nothing
    }

    private void drawItemColor(int centerViewPosition, LinearLayout parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View view = parent.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(mTextColor);
                ((TextView) view).setTextSize(mTextSize);
                if (i == centerViewPosition) {
                    ((TextView) view).setTextColor(mAxisTextColor);
                    ((TextView) view).setTextSize(mAxisTextSize);
                }
            }
        }
    }

    private static final class YearWheelAdapter extends AbstractWheelTextAdapter {

        List<Integer> mYearIntList = new ArrayList<>();

        List<String> mYearStringList = new ArrayList<>();

        private int mStartYear;

        private int mEndYear;

        private float mTextSize;

        private int mTextColor;

        @Override
        protected float getDefaultTextSize() {
            return mTextSize;
        }

        @Override
        protected int getDefaultTextColor() {
            return mTextColor;
        }

        YearWheelAdapter(Context context, int startYear, int endYear, float textSize, int textColor) {
            super(context);
            mTextColor = textColor;
            mTextSize = textSize;
            mStartYear = startYear;
            mEndYear = endYear;
            for (int i = startYear; i <= endYear; i++) {
                mYearIntList.add(i);
                mYearStringList.add(context.getString(R.string.label_year_selection_formatter, i));
            }
        }

        YearWheelAdapter(Context context, Integer[] yearArray, float textSize, int textColor) {
            super(context);
            mTextColor = textColor;
            mTextSize = textSize;
            mYearIntList.clear();
            if (yearArray != null) {
                mYearIntList = Arrays.asList(yearArray);
                mStartYear = yearArray[0];
                mEndYear = yearArray[yearArray.length - 1];
                for (Integer year : yearArray) {
                    mYearStringList.add(context.getString(R.string.label_year_selection_formatter, year));
                }
            }
        }

        @Override
        protected CharSequence getItemText(int index) {
            return mYearStringList.get(index);
        }

        @Override
        public int getItemsCount() {
            return mYearIntList.size();
        }

        public int getYearPosition(int year) {
            int position;
            if (year < mStartYear) {
                position = 0;
            } else if (year > mEndYear) {
                position = mYearIntList.size() - 1;
            } else {
                position = mYearIntList.indexOf(year);
            }
            return position;
        }

        /**
         * 获得选中的年份的int值.
         *
         * @param position 年份控件选中的位置
         * @return 年份
         */
        public int getYear(int position) {
            return mYearIntList.get(position);
        }
    }

    private static class SecondWheelAdapter extends AbstractWheelTextAdapter {

        private static final int MAX_HOURS = 60;

        Resources mResources;
        private float mTextSize;
        private int mTextColor;

        @Override
        protected float getDefaultTextSize() {
            return mTextSize;
        }

        @Override
        protected int getDefaultTextColor() {
            return mTextColor;
        }

        SecondWheelAdapter(Context context, float textSize, int textColor) {
            super(context);
            mTextColor = textColor;
            mTextSize = textSize;
            mResources = context.getResources();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return mResources.getString(R.string.label_second, index);
        }

        @Override
        public int getItemsCount() {
            return MAX_HOURS;
        }
    }

    private static class CustomizationMaxItemWheelAdapter extends AbstractWheelTextAdapter {

        List<String> mItemList;

        int mMaxItemCount;
        private float mTextSize;
        private int mTextColor;
        /**
         * 根据传入的最小时间，低于该时间的位置不在显示.
         */
        private int mUnShowCount;

        CustomizationMaxItemWheelAdapter(Context context, String[] itemArray, float textSize,
                int textColor) {
            super(context);
            mTextColor = textColor;
            mTextSize = textSize;
            mItemList = Arrays.asList(itemArray);
            mMaxItemCount = mItemList.size();
        }

        @Override
        public View getEmptyItem(View convertView, ViewGroup parent) {
            View view = super.getEmptyItem(convertView, parent);
            //初始化空item
            if (view != null && view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText("");
            }
            return view;
        }

        /**
         * 获取当前未显示数据的偏移量.
         *
         * @return 偏移量
         */
        private int getUnShowCount() {
            return mUnShowCount;
        }

        @Override
        public int getItemsCount() {
            return mMaxItemCount - mUnShowCount;
        }

        /**
         * 更新最大item数量.
         *
         * @param maxItemCount 最大item数量
         * @param unShowCount  数据列表不显示的条数）
         */
        public void updateViewItem(int maxItemCount, int unShowCount) {
            mMaxItemCount = maxItemCount;
            mUnShowCount = unShowCount;
            if (mMaxItemCount >= unShowCount) {
                notifyDataChangedEvent();
            }
        }


        @Override
        protected CharSequence getItemText(int index) {
            return mItemList.get(index + mUnShowCount);
        }

        @Override
        protected float getDefaultTextSize() {
            return mTextSize;
        }

        @Override
        protected int getDefaultTextColor() {
            return mTextColor;
        }

    }

}
