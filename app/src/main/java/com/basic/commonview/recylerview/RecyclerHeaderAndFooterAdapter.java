package com.basic.commonview.recylerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


/**
 *unComplete.
 */
public class RecyclerHeaderAndFooterAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Adapter mAdapter;
    private List<FixedViewInfo> mHeaderViewList;
    private List<FixedViewInfo> mFooterViewList;
    private static final int ADJUST_VIEW_TYPE_POSITION = 1;  //从1开始计算类型位置.


    public RecyclerHeaderAndFooterAdapter(List<FixedViewInfo> headerViewList, List<FixedViewInfo> footerViewList,
            Adapter adapter) {
        mAdapter = adapter;
        mHeaderViewList = headerViewList;
        mFooterViewList = footerViewList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //这里的viewType要做矫正.
        //header
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //这里需要做矫正.
        mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
       //进行校正
        mAdapter.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mHeaderViewList != null) {
            itemCount = mHeaderViewList.size();
        }
        if (mFooterViewList != null) {
            itemCount += mFooterViewList.size();
        }
        return itemCount + mAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaderViewList.size()) {
            return adjustHeaderType(position);
        } else if (position < mAdapter.getItemCount() + mHeaderViewList.size()) {
            return adjustAdapterType(position);
        } else {
            return adjustFooterType(position);
        }
    }

    private int adjustHeaderType(int position) {
        return ADJUST_VIEW_TYPE_POSITION + position;
    }

    private int adjustAdapterType(int position) {
        return ADJUST_VIEW_TYPE_POSITION + mHeaderViewList.size() + mAdapter.getItemViewType(position);
    }

    private int adjustFooterType(int position) {
        return ADJUST_VIEW_TYPE_POSITION + mAdapter.getItemViewType(position) + position + 1 - mAdapter.getItemCount();
    }

    /**
     * 判断数据是否来自于子类的adater.
     *
     * @param position
     * @return
     */
    public boolean isDataFromAdapter(int position) {
        if (position > mHeaderViewList.size() && position < getItemCount() - mFooterViewList.size()) {
            return true;
        }
        return false;
    }

    private class HeaderHolder extends ViewHolder {
        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }
}
