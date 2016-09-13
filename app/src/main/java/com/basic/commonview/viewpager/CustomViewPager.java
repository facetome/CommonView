package com.basic.commonview.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.basic.commonview.R;
import com.basic.commonview.util.LogUtils;

import java.lang.reflect.Field;

/**
 * 自定义ViewPager.
 * 暂时关闭了左右滑动切换页面的功能.
 */
public class CustomViewPager extends ViewPager {

    private boolean mEnableSwitch = false;
    private static final String TAG = "CustomViewPager";

    /**
     * 构造器.
     *
     * @param context {@link android.content.Context}
     */
    public CustomViewPager(Context context) {
        super(context);
    }

    /**
     * 构造器.
     *
     * @param context {@link android.content.Context}
     * @param attrs   {@link android.util.AttributeSet}
     */
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomViewPager);
        mEnableSwitch = a.getBoolean(R.styleable.CustomViewPager_enableSwitchByGesture, false);
        a.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mEnableSwitch && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mEnableSwitch && super.onInterceptTouchEvent(event);
    }

    /**
     * 设置是否可以滑动.
     *
     * @param enable true or false
     */
    public void setEnableSwitch(boolean enable) {
        mEnableSwitch = enable;
    }

    /**
     * viewPger滑动速度控制器.
     */
    public static class PagerScroller extends Scroller {

        private int mCustomDuration;

        /**
         * 构造函数.
         *
         * @param context {@link Context}
         */
        public PagerScroller(Context context) {
            super(context);
        }

        /**
         * 构造函数.
         *
         * @param context      {@link Context}
         * @param interpolator {@link Interpolator}
         */
        public PagerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        /**
         * 构造函数.
         *
         * @param context      {@link Context}
         * @param interpolator {@link Interpolator}
         * @param flywheel     Specify whether or not to support progressive "flywheel" behavior flinging
         */
        public PagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        /**
         * 设置滑动时间.
         *
         * @param duration time
         */
        public void setScrollDuration(int duration) {
            mCustomDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mCustomDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mCustomDuration);
        }

        /**
         * 初始化viewPager.
         *
         * @param pager {@link CustomViewPager}
         */
        public void initViewPagerScroll(CustomViewPager pager) {
            try {
                Field mScroller = ViewPager.class.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                mScroller.set(pager, this);
            } catch (NoSuchFieldException e) {
                LogUtils.e(TAG, e);
            } catch (IllegalAccessException e) {
                LogUtils.e(TAG, e);
            }
        }
    }
}

