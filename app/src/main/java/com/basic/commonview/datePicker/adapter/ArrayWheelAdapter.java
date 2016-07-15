/*
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
package com.basic.commonview.datePicker.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * The simple Array wheel adapter.
 *
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter {

    // items
    private T[] mItems;

    private float mDefaultTextSize;
    private int mDefaultTextColor;
    private static final int VIEW_PADDING = 8;

    /**
     * Constructor.
     *
     * @param context   the current context
     * @param items     the items
     * @param textColor default text Color.
     * @param textSize  default text size.
     */
    public ArrayWheelAdapter(Context context, T[] items, int textColor, float textSize) {
        super(context);
        mItems = items.clone();
        mDefaultTextColor = textColor;
        mDefaultTextSize = textSize;
    }

    @Override
    protected float getDefaultTextSize() {
        return mDefaultTextSize;
    }

    @Override
    protected int getDefaultTextColor() {
        return mDefaultTextColor;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < mItems.length) {
            T item = mItems[index];
            if (item instanceof CharSequence) {
                return (CharSequence) item;
            }
            return item.toString();
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return mItems.length;
    }

    @Override
    protected void configureTextView(TextView view) {
        super.configureTextView(view);
        view.setPadding(0, VIEW_PADDING, 0, VIEW_PADDING);
        view.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
    }
}
