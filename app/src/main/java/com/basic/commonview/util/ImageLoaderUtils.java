package com.basic.commonview.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;

/**
 * 图片加载工具类.
 */
public final class ImageLoaderUtils {

    /**
     * 图片更新完毕状态监听.
     */
    public interface UpdateFinishedListener {
        /**
         * 更新完毕监听.
         *
         * @param url        新的URL.
         * @param bitmap     获得的Bitmap.
         * @param imageAware ImageView.
         * @param loadedFrom {@link LoadedFrom}
         */
        void onUpdateFinish(String url, Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom);
    }

    private ImageLoaderUtils() {
        // make it as a private class.
    }

    /**
     * 使用currentUrl加载图片.
     *
     * @param imageView  显示ImageView.
     * @param iconConfig {@link IconConfig}.
     */
    public static void loaderImageByCurrentUrl(final ImageView imageView, final IconConfig iconConfig) {
        if (!TextUtils.isEmpty(iconConfig.getCurrentUrl())) {
            Builder builder = getDefaultOptionsBuilder(iconConfig);
            builder.displayer(new BitmapDisplayer() {
                @Override
                public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                    if (loadedFrom != LoadedFrom.NETWORK) {
                        imageAware.setImageBitmap(bitmap);
                    }
                }
            });
            ImageLoader.getInstance().displayImage(iconConfig.getCurrentUrl(), imageView, builder.build());
        }
    }

    /**
     * 使用updateUrl预加载图片.
     *
     * @param iconConfig {@link IconConfig}.
     * @param listener   更新成功
     */
    public static void updateImageByUpdateUrl(final IconConfig iconConfig, final UpdateFinishedListener listener) {
        if (!TextUtils.isEmpty(iconConfig.getUpdateUrl()) && !iconConfig.getUpdateUrl()
                .equals(iconConfig.getCurrentUrl())) {
            Builder builder = getDefaultBuilder();
            ImageLoader.getInstance().loadImage(iconConfig.getUpdateUrl(), builder.build(),
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (listener != null) {
                                listener.onUpdateFinish(iconConfig.getUpdateUrl(), loadedImage, null, LoadedFrom.NETWORK);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
        }
    }

    /**
     * 加载图片.
     *
     * @param imageView  显示ImageView.
     * @param iconConfig {@see IconConfig}
     * @param options    {@link DisplayImageOptions}
     */
    public static void loaderImage(ImageView imageView, IconConfig iconConfig,
            DisplayImageOptions options) {
        loaderImage(imageView, iconConfig.getCurrentUrl(), options);
    }

    /**
     * 加载图片.
     *
     * @param imageView 显示ImageView.
     * @param url       网络加载URL.
     * @param options   {@link DisplayImageOptions}
     */
    public static void loaderImage(ImageView imageView, String url, DisplayImageOptions options) {
        if (!TextUtils.isEmpty(url)) {
            ImageLoader.getInstance().displayImage(url, imageView, options);
        }
    }

    /**
     * 单纯的下载器使用.
     *
     * @param url     下载地址.
     * @param options {@link DisplayImageOptions}
     */
    public static void loadImage(String url, DisplayImageOptions options) {
        if (!TextUtils.isEmpty(url)) {
            ImageLoader.getInstance().loadImage(url, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    // do nothing
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    //do nothing
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // do nothing
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    // do nothing
                }
            });
        }
    }

    /**
     * 加载广告配置图片.
     *
     * @param imageView imageView
     * @param config    {@link IconConfig}
     */
    public static void loadAdvertiseImage(final ImageView imageView, final IconConfig config) {
        Builder builder = getDefaultOptionsBuilder(config);
        builder.displayer(new BitmapDisplayer() {
            @Override
            public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                if (config.isIsAutoUpdate()) {
                    // 自动更新，那么此时将下载出来的图片设置到界面上
                    imageAware.setImageBitmap(bitmap);
                }
            }
        });
        loaderImage(imageView, config, builder.build());
    }

    private static Builder getDefaultOptionsBuilder(IconConfig iconConfig) {
        Builder builder = new Builder();
        builder.cacheInMemory(true).cacheOnDisk(true);
        if (iconConfig.getLoadingRes() != 0) {
            builder.showImageOnLoading(iconConfig.getLoadingRes());
        }

        if (iconConfig.getEmptyRes() != 0) {
            builder.showImageForEmptyUri(iconConfig.getEmptyRes());
        }

        if (iconConfig.getFailedRes() != 0) {
            builder.showImageOnFail(iconConfig.getFailedRes());
        }
        return builder;
    }

    /**
     * 获取默认builder.
     *
     * @return {@link Builder}
     */
    public static Builder getDefaultBuilder() {
        Builder builder = new Builder();
        builder.cacheInMemory(true)
                .cacheOnDisk(true);
        return builder;
    }

    /**
     * 获取专用广告条配置.
     *
     * @param failImage 失败时的默认图片.
     * @return 通用配置
     */
    public static DisplayImageOptions getAdvertiseImageDisplayOptionsWithFailImage(int failImage) {
        return new Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(failImage)
                .build();
    }

    /**
     * 获取通用配置.
     *
     * @param defaultIcon 默认加载失败时候的资源图标.
     * @return 通用配置
     */
    public static DisplayImageOptions getImageDisplayOptions(int defaultIcon) {
        return new Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .showImageOnFail(defaultIcon)
                .showImageOnLoading(defaultIcon)
                .showImageForEmptyUri(defaultIcon)
                .build();
    }

    /**
     * 获取通用配置.
     *
     * @param defaultIcon 默认加载失败时候的资源图标.
     * @param radius      图片圆角
     * @return 通用配置
     */
    public static DisplayImageOptions getRoundImageDisplayOptions(int defaultIcon, int radius) {
        RoundedBitmapDisplayer displayer = new RoundedBitmapDisplayer(radius);
        return new Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .displayer(displayer)
                .showImageOnFail(defaultIcon)
                .showImageOnLoading(defaultIcon)
                .showImageForEmptyUri(defaultIcon)
                .build();
    }

    /**
     * 清除指定地址的cache.
     *
     * @param url 图片url
     * @param targetSize 目标大小.
     */
    public static void clearMemoryCache(String url, ImageSize targetSize) {
        MemoryCache cache = ImageLoader.getInstance().getMemoryCache();
        String key = MemoryCacheUtils.generateKey(url, targetSize);
        cache.remove(key);
    }

    /**
     * is existing for url in local.
     *
     * @param url image url.
     * @return is existing or not.
     */
    public static boolean hasCacheInLocal(String url) {
        if (!TextUtils.isEmpty(url)) {
            File image = ImageLoader.getInstance().getDiskCache().get(url);
            return image != null && image.exists();
        }
        return false;
    }

    /**
     * 资源配置.
     */
    public static class IconConfig {

        private int mKeyType;

        private String mCurrentUrl;

        private String mUpdateUrl;

        private int mLoadingRes;

        private int mEmptyRes;

        private int mFailedRes;

        private boolean mIsAutoUpdate;

        public int getKeyType() {
            return mKeyType;
        }

        public void setKeyType(int keyType) {
            mKeyType = keyType;
        }

        public String getCurrentUrl() {
            return mCurrentUrl;
        }

        public void setCurrentUrl(String currentUrl) {
            mCurrentUrl = currentUrl;
        }

        public String getUpdateUrl() {
            return mUpdateUrl;
        }

        public void setUpdateUrl(String updateUrl) {
            mUpdateUrl = updateUrl;
        }

        public int getLoadingRes() {
            return mLoadingRes;
        }

        public void setLoadingRes(int loadingRes) {
            mLoadingRes = loadingRes;
        }

        public int getEmptyRes() {
            return mEmptyRes;
        }

        public void setEmptyRes(int emptyRes) {
            mEmptyRes = emptyRes;
        }

        public int getFailedRes() {
            return mFailedRes;
        }

        public void setFailedRes(int failedRes) {
            mFailedRes = failedRes;
        }

        public boolean isIsAutoUpdate() {
            return mIsAutoUpdate;
        }

        public void setAutoUpdate(boolean isAutoUpdate) {
            mIsAutoUpdate = isAutoUpdate;
        }
    }
}
