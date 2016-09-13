package com.basic.commonview.recylerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现加headerView的GridView.
 * 暂时未考虑没有添加header的情况，后续慢慢添加.
 * 当前支持list and grid.
 */
public class GridRecyclerHeaderView extends RecyclerView {

    private List<FixedViewInfo> mHeaderList = new ArrayList<>();
    private List<FixedViewInfo> mFooterList = new ArrayList<>();
    private RecyclerHeaderAndFooterAdapter mAdapter;

    public GridRecyclerHeaderView(Context context) {
        super(context);
    }

    public GridRecyclerHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridRecyclerHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addHeaderView(View view) {
        Adapter adapter = getAdapter();
        if (adapter != null && !(adapter instanceof RecyclerHeaderAndFooterAdapter)) {
            throw new UnsupportedOperationException("method addHeaderView must be called before setAdapter");
        }
        if (view != null) {
            FixedViewInfo info = new FixedViewInfo();
            info.mView = view;
            mHeaderList.add(info);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void addFooterView(View view) {
        Adapter adapter = getAdapter();
        if (adapter != null && !(adapter instanceof RecyclerHeaderAndFooterAdapter)) {
            throw new UnsupportedOperationException("method addFooterView must be called before setAdapter");
        }

        if (view != null) {
            FixedViewInfo info = new FixedViewInfo();
            info.mView = view;
            mFooterList.add(info);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void removeHeader(View header) {
        // TODO
    }

    public void removeFooter(View footer) {
        // TODO
    }


    @Override
    public void setAdapter(Adapter adapter) {
        if (mHeaderList.size() > 0 || mFooterList.size() > 0) {
            final RecyclerHeaderAndFooterAdapter newAdapter
                    = new RecyclerHeaderAndFooterAdapter(mHeaderList, mFooterList, adapter);
            final LayoutManager manager = getLayoutManager();
            if (manager instanceof GridLayoutManager && ((GridLayoutManager) manager).getOrientation()
                    == GridLayoutManager.VERTICAL) {
                ((GridLayoutManager) manager).setSpanSizeLookup(new SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (!newAdapter.isDataFromAdapter(position)) {
                            return ((GridLayoutManager) manager).getSpanCount();
                        }
                        return 1;
                    }
                });
            }
            super.setAdapter(newAdapter);
        } else {
            super.setAdapter(adapter);
        }
    }
}
