package com.basic.commonview.recylerview.itemDecoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * item装饰框.
 */
public class SimpleItemDecoration extends ItemDecoration {
    private Drawable mDivider;
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    public SimpleItemDecoration(Context context) {
        TypedArray array = context.obtainStyledAttributes(ATTRS);
        mDivider = array.getDrawable(0);
        array.recycle();
    }

    /**
     * 设置自定义的divider.
     *
     * @param divider
     */
    public void sestItemDivider(Drawable divider) {
        mDivider = divider;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        LayoutManager manager = parent.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            drawHorizontal(c, parent);
        } else if (manager instanceof LinearLayoutManager){
            drawVertical(c, parent);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        buildOutDivider(outRect, parent, itemPosition);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        //该方法实现过程
        // 系统将outRect第位置信息计算到了item view 的padding中，当进行偏移的时候，导致了item在绘制的时候不能充满屏幕，那此时将就分割线会显示出来了.
        int position = parent.getChildLayoutPosition(view);
        buildOutDivider(outRect, parent, position);
    }

    private void buildOutDivider(Rect outrRect, RecyclerView parent, int position) {
        // 第一行和最后一张均不绘制分割线.
        if (!isDrawHorizontal(parent, position)) {
            outrRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else if (!isDrawVertical(parent, position)) {
            outrRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        } else {
            outrRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
        }
    }

    private boolean isDrawVertical(RecyclerView parent, int position) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getChildCount();
        int rawCount = (childCount - 1) / spanCount; //计算行数.第一行已经最后一行不画divider
        if (position == 0 || (position - 1) / spanCount == rawCount) {
            return false;
        }
        return true;
    }

    private boolean isDrawHorizontal(RecyclerView parent, int position) {
        int spanCount = getSpanCount(parent);
        if (position == 0 || position % spanCount == 0) {  //header和最后一列不添加divider.
            return false;
        }
        return true;
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        int count = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int spanCount = getSpanCount(parent);
        for (int i = 0; i < count; i++) {
            if (i % spanCount == 0) {
                View child = parent.getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
    }

    /**
     * 计算列数.
     *
     * @param parent
     * @return
     */
    private int getSpanCount(RecyclerView parent) {
        LayoutManager manager = parent.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            return ((GridLayoutManager) manager).getSpanCount();
        }
        return 0;
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int count = parent.getChildCount();
        int top = parent.getTop() + parent.getPaddingTop();
        int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int left = child.getRight() + params.rightMargin;
            int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }
}