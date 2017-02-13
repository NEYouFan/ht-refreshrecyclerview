/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.netease.hearttouch.htrefreshrecyclerview.utils.Utils;

import static android.R.attr.orientation;

/**
 * 刷新视图和加载更多视图的包裹基类,用户需要继承该类完成自定义视图样式
 */
public abstract class HTBaseViewHolder implements HTBaseRecyclerView.HTLoadMoreUIChangeListener, HTBaseRecyclerView.HTRefreshUIChangeListener {

    private static final int DEFAULT_ANIMATION_TIME = 500;
    private static final int DEFAULT_VIEW_SIZE = 50;

    protected Context mContext;
    /**
     * 刷新视图对象
     */
    protected View mRefreshView;
    /**
     * 加载更多视图对象
     */
    protected View mLoadMoreView;
    /**
     * 刷新视图的背景色
     */
    private int mRefreshViewBackgroundResId = 0;
    /**
     * 加载更多视图的背景色
     */
    private int mLoadMoreViewBackgroundResId = 0;
    /**
     * 动画时间，默认500ms
     */
    private int mAnimationTime = DEFAULT_ANIMATION_TIME;

    private HTViewHolderTracker mViewHolderTracker;

    private int mOrientation = 1;

    public HTBaseViewHolder(Context context) {
        mContext = context;
        mRefreshView = onInitRefreshView();
        mLoadMoreView = onInitLoadMoreView();
        mViewHolderTracker = new HTViewHolderTracker();
    }

    /**
     * 设置加载更多视图的背景色
     *
     * @param loadMoreViewBackgroundResId 背景色资源Id
     */
    public void setLoadMoreViewBackgroundResId(int loadMoreViewBackgroundResId) {
        if (loadMoreViewBackgroundResId > 0) {
            this.mLoadMoreViewBackgroundResId = loadMoreViewBackgroundResId;
            if (mLoadMoreView != null) {
                ViewGroup container = (ViewGroup) mLoadMoreView.getParent();
                if (container != null) {
                    container.setBackgroundResource(loadMoreViewBackgroundResId);
                }
            }
        }
    }

    protected void updateViewSize() {
        computeViewSize(orientation);
    }

    /**
     *  计算刷新视图和加载更多视图在刷新方向上的尺寸
     * @param orientation
     */
    protected void computeViewSize(int orientation) {
        mOrientation = orientation;
        int refreshSize = mRefreshView == null ? 0 : Utils.getItemViewSize(orientation, mRefreshView);
        int loadMoreSize = mRefreshView == null ? 0 : Utils.getItemViewSize(orientation, mLoadMoreView);
        mViewHolderTracker.setRefreshViewSize(refreshSize == 0 ? DEFAULT_VIEW_SIZE : refreshSize);
        mViewHolderTracker.setLoadMoreSize(loadMoreSize == 0 ? DEFAULT_VIEW_SIZE : loadMoreSize);
    }


    /**
     * 设置刷新视图的背景色
     *
     * @param refreshViewBackgroundResId 背景色资源Id
     */
    public void setRefreshViewBackgroundResId(int refreshViewBackgroundResId) {
        if (refreshViewBackgroundResId > 0) {
            mRefreshViewBackgroundResId = refreshViewBackgroundResId;
            if (mRefreshView != null) {
                ViewGroup container = (ViewGroup) mRefreshView.getParent();
                if (container != null) {
                    container.setBackgroundResource(refreshViewBackgroundResId);
                }
            }
        }
    }


    public HTViewHolderTracker getViewHolderTracker() {
        return mViewHolderTracker;
    }


    /**
     * 设置刷新和加载更多相关动画的执行时间
     *
     * @param animationTime 动画时间,单位ms
     */
    public void setAnimationTime(int animationTime) {
        if (animationTime < 0) {
            throw new IllegalArgumentException("the parameter value should be greater than 0 !");
        }
        mAnimationTime = animationTime;
    }

    /**
     * 设置手指滑动距离与刷新控件移动距离的比值
     *
     * @param pullDistanceScale 比例值,默认1.8f
     */
    public void setPullDistanceScale(float pullDistanceScale) {
        if (pullDistanceScale < 0) {
            throw new IllegalArgumentException("the parameter value should be greater than 0 !");
        }
        mViewHolderTracker.setPullDistanceScale(pullDistanceScale);
    }

    /**
     * 设置触发刷新控件事件的弹簧距离，其值是与刷新控件高度的比值
     *
     * @param springDistanceScale 比例值,默认1.0f
     */
    public void setSpringDistanceScale(float springDistanceScale) {
        if (springDistanceScale < 1.0) {
            throw new IllegalArgumentException("the parameter value should be greater than 1.0 !");

        }
        mViewHolderTracker.setSpringDistanceScale(springDistanceScale);
    }

    /**
     * 自定义的刷新视图
     */
    public abstract View onInitRefreshView();

    /**
     * 自定义的加载更多视图
     */
    public abstract View onInitLoadMoreView();

    public final int getAnimationTime() {
        return mAnimationTime;
    }

    public final int getLoadMoreViewBackgroundResId() {
        return mLoadMoreViewBackgroundResId;
    }

    public final int getRefreshViewBackgroundResId() {
        return mRefreshViewBackgroundResId;
    }

    public final View getRefreshView() {
        return mRefreshView;
    }

    public final View getLoadMoreView() {
        return mLoadMoreView;
    }

}
