/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.basic.commonview.datePicker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates .
 */
public class WheelScroller {
    /**
     * Scrolling listener interface.
     */
    public interface ScrollingListener {
        /**
         * Scrolling callback called when scrolling is performed.
         *
         * @param distance the distance to scroll
         */
        void onScroll(int distance);

        /**
         * Starting callback called when scrolling is started.
         */
        void onStarted();

        /**
         * Finishing callback called after justifying.
         */
        void onFinished();

        /**
         * Justifying callback called to justify a view when scrolling is ended.
         */
        void onJustify();
    }

    /**
     * Scrolling duration.
     */
    private static final int SCROLLING_DURATION = 400;

    /**
     * Minimum delta for scrolling.
     */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    // Listener
    private ScrollingListener mListener;

    // Context
    private Context mContext;

    // Scrolling
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private int mLastScrollY;
    private float mLastTouchedY;
    private boolean misScrollingPerformed;

    /**
     * Constructor.
     *
     * @param context  the current context
     * @param listener the scrolling listener
     */
    public WheelScroller(Context context, ScrollingListener listener) {
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);

        mScroller = new Scroller(context);

        mListener = listener;
        mContext = context;
    }

    /**
     * Set the the specified scrolling interpolator.
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mScroller.forceFinished(true);
        mScroller = new Scroller(mContext, interpolator);
    }

    /**
     * Scroll the wheel.
     *
     * @param distance the scrolling distance
     * @param time     the scrolling duration
     */
    public void scroll(int distance, int time) {
        mScroller.forceFinished(true);
        mLastScrollY = 0;
        mScroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);
        startScrolling();
    }

    /**
     * Stops scrolling.
     */
    public void stopScrolling() {
        mScroller.forceFinished(true);
    }

    /**
     * Handles Touch event.
     *
     * @param event the motion event
     * @return touching event.
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchedY = event.getY();
                mScroller.forceFinished(true);
                clearMessages();
                break;

            case MotionEvent.ACTION_MOVE:
                // perform scrolling
                int distanceY = (int) (event.getY() - mLastTouchedY);
                if (distanceY != 0) {
                    startScrolling();
                    mListener.onScroll(distanceY);
                    mLastTouchedY = event.getY();
                }
                break;
            default:
                break;
        }

        if (!mGestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }

        return true;
    }

    // gesture listener
    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Do scrolling in onTouchEvent() since onScroll() are not call immediately
            //  when user touch and move the wheel
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mLastScrollY = 0;
            final int maxY = 0x7FFFFFFF;
            final int minY = -maxY;
            mScroller.fling(0, mLastScrollY, 0, (int) -velocityY, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    private static final int MESSAGE_SCROLL = 0;
    private static final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        mAnimationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue.
     */
    private void clearMessages() {
        mAnimationHandler.removeMessages(MESSAGE_SCROLL);
        mAnimationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    private Handler mAnimationHandler = new Handler() {
        public void handleMessage(Message msg) {
            mScroller.computeScrollOffset();
            int currY = mScroller.getCurrY();
            int delta = mLastScrollY - currY;
            mLastScrollY = currY;
            if (delta != 0) {
                mListener.onScroll(delta);
            }

            // scrolling is not finished when it comes to final Y.
            // so, finish it manually.
            if (Math.abs(currY - mScroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                mScroller.forceFinished(true);
            }
            if (!mScroller.isFinished()) {
                mAnimationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel.
     */
    private void justify() {
        mListener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY);
    }

    /**
     * Starts scrolling.
     */
    private void startScrolling() {
        if (!misScrollingPerformed) {
            misScrollingPerformed = true;
            mListener.onStarted();
        }
    }

    /**
     * Finishes scrolling.
     */
    void finishScrolling() {
        if (misScrollingPerformed) {
            mListener.onFinished();
            misScrollingPerformed = false;
        }
    }
}
