/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.content.Context;
import android.view.View;

/**
 * 刷新视图和加载更多视图的包裹基类,用户需要继承该类完成自定义视图样式
 */
public abstract class HTBaseViewHolder implements HTBaseRecyclerView.HTLoadMoreUIChangeListener, HTBaseRecyclerView.HTRefreshUIChangeListener {

    private static final float PULL_DISTANCE_SCALE = 1.8f;
    private static final float SPRING_DISTANCE_SCALE = 2.4f;
    private static final int DEFAULT_ANIMATION_TIME = 500;

    protected Context mContext;
    /** 刷新视图对象 */
    protected View mRefreshView;
    /** 加载更多视图对象 */
    protected View mLoadMoreView;
    /** 刷新视图的背景色 */
    private int mRefreshViewBackgroundResId = 0;
    /** 加载更多视图的背景色 */
    private int mLoadMoreViewBackgroundResId = 0;
    /** 手指移动距离与刷新控件移动距离的比值,默认1.8f */
    private float mPullDistanceScale = PULL_DISTANCE_SCALE;
    /** 刷新控件弹簧距离与刷新控件高度的比值,默认2.4f */
    private float mSpringDistanceScale = SPRING_DISTANCE_SCALE;
    /** 动画时间，默认500ms */
    private int mAnimationTime = DEFAULT_ANIMATION_TIME;

    public HTBaseViewHolder(Context context) {
        mContext = context;
        mRefreshView = onInitRefreshView();
        mLoadMoreView = onInitLoadMoreView();
    }

    /**
     * 设置加载更多视图的背景色
     * @param loadMoreViewBackgroundResId 背景色资源Id
     */
    public void setLoadMoreViewBackgroundResId(int loadMoreViewBackgroundResId) {
        if (loadMoreViewBackgroundResId > 0) {
            this.mLoadMoreViewBackgroundResId = loadMoreViewBackgroundResId;
            if (mLoadMoreView != null) {
                mLoadMoreView.setBackgroundResource(loadMoreViewBackgroundResId);
            }
        }
    }

    /**
     * 设置刷新视图的背景色
     * @param refreshViewBackgroundResId 背景色资源Id
     */
    public void setRefreshViewBackgroundResId(int refreshViewBackgroundResId) {
        if (refreshViewBackgroundResId > 0) {
            mRefreshViewBackgroundResId = refreshViewBackgroundResId;
            if (mRefreshView != null) {
                mRefreshView.setBackgroundResource(refreshViewBackgroundResId);
            }
        }
    }

    /**
     * 设置刷新和加载更多相关动画的执行时间
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
     * @param pullDistanceScale 比例值,默认1.8f
     */
    public void setPullDistanceScale(float pullDistanceScale) {
        if (pullDistanceScale < 0) {
            throw new IllegalArgumentException("the parameter value should be greater than 0 !");
        }
        mPullDistanceScale = pullDistanceScale;
    }

    /**
     * 设置刷新控件弹簧距离与刷新控件高度的比值
     * @param springDistanceScale 比例值,默认2.4f
     */
    public void setSpringDistanceScale(float springDistanceScale) {
        if (springDistanceScale < 0) {
            throw new IllegalArgumentException("the parameter value should be greater than 0 !");

        }
        mSpringDistanceScale = springDistanceScale;
    }

    /** 自定义的刷新视图 */
    public abstract View onInitRefreshView();

    /** 自定义的加载更多视图 */
    public abstract View onInitLoadMoreView();

    public final float getSpringDistanceScale() {
        return mSpringDistanceScale;
    }

    public final float getPullDistanceScale() {
        return mPullDistanceScale;
    }

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
