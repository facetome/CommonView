package com.basic.commonview.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.basic.commonview.R;
import com.basic.commonview.util.ImageLoaderUtils;
import com.basic.commonview.util.Utility;
import com.basic.commonview.viewpager.CustomViewPager.PagerScroller;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义viewPager容器，主要用于图片的轮播.
 * 循环滚动的具体思路，将最后一个view放在数据表中的第一位置，第一个放在最后一个位置.
 */
public class ViewPagerContainer extends RelativeLayout implements Runnable, OnPageChangeListener {

    /**
     * 用于监听页面点击.
     */
    public interface OnPagerItemClickListener {
        /**
         * 点击事件.
         *
         * @param position position
         * @param entity   {@link ImageEntity}
         */
        void onPagerClick(int position, ImageEntity entity);
    }

    public static final String TAG = "ViewPagerContainer";
    private CustomViewPager mViewPager;
    private ImagePagerAdapter mAdapter;
    private OnPagerItemClickListener mItemClickListener;
    private int mCurrentPosition = 0;
    private Context mContext;
    private static final long TIME_DELAY = 4000;
    private static final int PAGE_LIMIT = 1;
    private static final int TIME_SMOOTH = 1000;
    private static final int TIME_USER_SLIDING = 2000;
    private static final int MARGIN_INDICATOR = 5;
    private static final int MIN_CIRCLE_PAGE_SIZE = 2;
    private static final int DEFAULT_IMAGE_FACTOR = 2;
    private LinearLayout mIndicatorContainer;
    private boolean mCircle = false;
    private long mLastTime;
    private boolean mIsScrolling = false;
    private int mDefaultImage;
    private long mCircleTime = TIME_DELAY;
    private int mPagerLimit = PAGE_LIMIT;
    private float mImageFactor = DEFAULT_IMAGE_FACTOR;

    /**
     * 构造函数.
     *
     * @param context {@link Context}
     * @param attrs   {@link AttributeSet}
     */
    public ViewPagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerContainer);
        mImageFactor = array.getFloat(R.styleable.ViewPagerContainer_pager_factor, DEFAULT_IMAGE_FACTOR);
        array.recycle();
        mContext = context;
        initView(context);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = Utility.getScreenWidth(mContext);
        int height = (int) (width / mImageFactor);
        int pagerHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, pagerHeight);
    }

    @Override
    public void run() {
        if (mAdapter.getCount() >= MIN_CIRCLE_PAGE_SIZE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime >= TIME_USER_SLIDING) {
                if (!mIsScrolling) {
                    int pageSize = mAdapter.getCount();
                    int offset = (mCurrentPosition + 1) % pageSize;
                    mViewPager.setCurrentItem(offset, true);
                }
            }
            postDelayed(this, mCircleTime);
        } else {
            stopPlay();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewPager.removeOnPageChangeListener(this);
        stopPlay();
    }

    private void initView(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.view_pager_fragment, this);
        mViewPager = (CustomViewPager) root.findViewById(R.id.view_pager);
        mIndicatorContainer = (LinearLayout) root.findViewById(R.id.page_indicator);
        mViewPager.setOffscreenPageLimit(mPagerLimit);
        PagerScroller scroll = new PagerScroller(context);
        scroll.setScrollDuration(TIME_SMOOTH);
        scroll.initViewPagerScroll(mViewPager);
        mViewPager.addOnPageChangeListener(this);
        mAdapter = new ImagePagerAdapter();
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // do nothing
    }

    @Override
    public void onPageSelected(int position) {
        int max = mAdapter.getCount() - 1;
        mCurrentPosition = position;
        if (mCircle) {
            if (position == max) {
                mCurrentPosition = 1;
            } else if (position == 0) {
                mCurrentPosition = max - 1;
            }
            setIndicatorSelect(position - 1);
        } else {
            setIndicatorSelect(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mIsScrolling = false;
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
            mIsScrolling = true;
        }
        if (mCircle && state == ViewPager.SCROLL_STATE_IDLE) {
            mLastTime = System.currentTimeMillis();
            mViewPager.setCurrentItem(mCurrentPosition, false);
        }
    }

    /**
     * 刷新数据.
     *
     * @param list 数据集合
     */
    public void onRefreshData(List<ImageEntity> list) {
        //只有一条数据或者没有数据时，不循环.
        if (isImgArrayChanged(list)) {
            if (list == null || list.size() <= 1) {
                mCircle = false;
                mViewPager.setEnableSwitch(false);
            } else {
                mCircle = true;
                mViewPager.setEnableSwitch(true);
            }
            if (mDefaultImage == 0) {
                throw new UnsupportedOperationException("you must set the default drawable resource "
                        + "id by method setPagerDefaultImage before call this method");
            }
            mAdapter.onRefreshData(list);
            buildIndicator(list == null ? 0 : list.size());
            mViewPager.setCurrentItem(1, false);
        }
    }

    private boolean isImgArrayChanged(List<ImageEntity> newList) {
        List<ImageEntity> oldList = mAdapter.getUrlList();
        int oldSize = oldList.size();
        if (newList != null && newList.size() == oldList.size()) {
            for (int i = 0; i < oldSize; i++) {
                ImageEntity newEntity = newList.get(i);
                ImageEntity oldEntity = oldList.get(i);
                if (newEntity.getImageUrl().equals(oldEntity.getImageUrl()) && newEntity
                        .getImageDirectUrl().equals(oldEntity.getImageDirectUrl())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 开始播放.
     */
    public void startPlay() {
        removeCallbacks(this);
        postDelayed(this, mCircleTime);
    }

    /**
     * onResume.
     */
    public void onResume() {
        startPlay();
    }

    /**
     * onPause.
     */
    public void onPause() {
        stopPlay();
    }

    /**
     * 停止播放.
     */
    public void stopPlay() {
        removeCallbacks(this);
    }

    /**
     * 设置默认图片.
     *
     * @param resourceId z图片资源id.
     */
    public void setPagerDefaultImage(int resourceId) {
        mDefaultImage = resourceId;
    }

    /**
     * 设置轮播时间.
     *
     * @param time 每一页的跳转时间.
     */
    public void setCircleDuration(long time) {
        mCircleTime = time;
    }

    /**
     * 销毁view,回收缓存.
     */
    public void onDestroy() {
        stopPlay();
        //清除缓存.
        List<ImageEntity> urlList = mAdapter.getUrlList();
        ImageSize size = new ImageSize(getWidth(), getHeight());
        for (ImageEntity entity : urlList) {
            ImageLoaderUtils.clearMemoryCache(entity.getImageUrl(), size);
        }
    }

    /**
     * 设置viewPager缓存页面.
     *
     * @param pagerLimit 缓存页面
     */
    public void setOffsetPagerLimit(int pagerLimit) {
        if (pagerLimit != mPagerLimit) {
            mViewPager.setOffscreenPageLimit(pagerLimit);
            mPagerLimit = pagerLimit;
        }
    }

    /**
     * 设置指示器位置.
     *
     * @param position {@link Gravity}
     */
    public void setIndicatorGravity(int position) {
        mIndicatorContainer.setGravity(position);
    }

    private void buildIndicator(int size) {
        mIndicatorContainer.removeAllViews();
        //要求一张图片不显示指示器.
        if (size > 1) {
            for (int index = 0; index < size; index++) {
                ImageView indicator = new ImageView(mContext);
                if (index == 0) {
                    indicator.setImageResource(R.drawable.ic_view_pager_select);
                } else {
                    indicator.setImageResource(R.drawable.ic_view_pager_normal);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                params.rightMargin = MARGIN_INDICATOR;
                mIndicatorContainer.addView(indicator, params);
            }
        }
    }

    private void setIndicatorSelect(int index) {
        int count = mIndicatorContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView indicator = (ImageView) mIndicatorContainer.getChildAt(i);
            if (indicator != null) {
                indicator.setImageResource(index == i ? R.drawable.ic_view_pager_select : R
                        .drawable.ic_view_pager_normal);
            }
        }
    }

    /**
     * 设置item点击监听器.
     *
     * @param listener {@link OnPagerItemClickListener}
     */
    public void setOnPagerItemClickListener(OnPagerItemClickListener listener) {
        if (listener != null) {
            mItemClickListener = listener;
        }
    }


    private class ImagePagerAdapter extends PagerAdapter {

        private List<ImageEntity> mAllImageEntityList = new ArrayList<>();

        private void onRefreshData(List<ImageEntity> urlData) {
            mAllImageEntityList.clear();
            if (urlData != null && urlData.size() > 0) {
                if (urlData.size() > 1) {
                    mAllImageEntityList.add(urlData.get(urlData.size() - 1));
                    for (ImageEntity entity : urlData) {
                        mAllImageEntityList.add(entity);
                    }
                    mAllImageEntityList.add(urlData.get(0));
                } else {
                    mAllImageEntityList.add(urlData.get(0));
                }
            }
            notifyDataSetChanged();
        }

        private List<ImageEntity> getUrlList() {
            return mAllImageEntityList;
        }

        //TODO 暂时使用该方式以保证imageLoader加载图片时能正确计算出加载到缓存中图片的大小后续实现一
        // TODO 个为UIL优化的可以预测自身大小的imageview，相关使用UIL的imageView都使用它.
        private ImageView assembleView(ImageEntity entity, ViewGroup parent) {
            ImageView image = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.truck_image_item, parent, false);
            image.setScaleType(ScaleType.CENTER_CROP);
            image.setMaxWidth(parent.getMeasuredWidth());
            image.setMaxHeight(parent.getMeasuredHeight());
            image.setTag(entity);
            return image;
        }

        @Override
        public int getCount() {
            int size = mAllImageEntityList.size();
            return size > 0 ? size : 1;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView image;
            if (mAllImageEntityList != null && mAllImageEntityList.size() > 0) {
                image = assembleView(mAllImageEntityList.get(position), container);
                ImageEntity entity = (ImageEntity) image.getTag();
                if (entity != null) {
                    if (TextUtils.isEmpty(entity.getImageUrl())) {
                        image.setImageResource(mDefaultImage);
                    } else {
                        ImageLoaderUtils.loaderImage(image, entity.getImageUrl(),
                                ImageLoaderUtils.getAdvertiseImageDisplayOptionsWithFailImage(mDefaultImage));
                    }
                } else {
                    image.setImageResource(mDefaultImage);
                }
            } else {
                image = assembleView(null, container);
                image.setImageResource(mDefaultImage);
            }
            final ImageEntity entity = (ImageEntity) image.getTag();
            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onPagerClick(position, entity);
                    }
                }
            });
            container.addView(image);
            return image;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        //该方法实现：当界面刷新切换的时候（notifyDataSetChanged），会判断上次缓存起来的item是否发生了位置变化，如果没发生，那么不会执行instantiateItem
        // ，否则会执行初始化操作，实现banner切换的关键就是要使清除缓存的时候重新执行初始化操作，那么我们根据上次缓存的位置的item进行判断，如果只有一个item
        // ，或者第一个显示的item是整个item列表的第一个，那么都返回 POSITION_NONE，让其执行那么不会执行instantiateItem。
        @Override
        public int getItemPosition(Object object) {
            ImageView imageView = (ImageView) object;
            ImageEntity entity = (ImageEntity) imageView.getTag();
            int size = mAllImageEntityList.size();
            if (entity != null) {
                if (size > 1) { //此时应该拿position == 1的位置的object
                    ImageEntity firstEntity = mAllImageEntityList.get(1);
                    if (firstEntity != null) {
                        return POSITION_NONE;
                    }
                } else {
                    return POSITION_NONE;
                }
            } else {
                //使用的默认图片时机.
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }

    /**
     * 用于界面上显示图片所用参数实体.
     */
    public static class ImageEntity {

        private String mImageUrl;
        private String mImageDirectUrl;

        public String getImageUrl() {
            return mImageUrl;
        }

        public void setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
        }

        public String getImageDirectUrl() {
            return mImageDirectUrl;
        }

        public void setImageDirectUrl(String imageDirectUrl) {
            mImageDirectUrl = imageDirectUrl;
        }
    }
}
