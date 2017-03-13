/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 刷新视图和加载更多视图的包裹基类,用户需要继承该类完成自定义视图样式
 */
public abstract class HTBaseViewHolder implements HTBaseRecyclerView.HTLoadMoreUIChangeListener, HTBaseRecyclerView.HTRefreshUIChangeListener {

    private static final int DEFAULT_ANIMATION_TIME = 500;

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
     * 包裹自定义刷新view的控件
     */
    private ViewGroup mRefreshContainerView;
    /**
     * 包裹自定义加载更多view的控件
     */
    private ViewGroup mLoadMoreContainerView;

    protected HTBaseRecyclerView mRecyclerView;
    /**
     * 刷新视图的背景色
     */
    private int mRefreshViewBackgroundResId = 0;

    /**
     * 动画时间，默认500ms
     */
    private int mAnimationTime = DEFAULT_ANIMATION_TIME;

    private HTViewHolderTracker mViewHolderTracker;

    public HTBaseViewHolder(Context context) {
        mContext = context;
        initViews(context);
    }

    private void initViews(Context context) {
        FrameLayout refreshLayout = new FrameLayout(context);
        mRefreshContainerView = (ViewGroup) onInitRefreshView(refreshLayout);
        LinearLayout loadMoreLayout = new LinearLayout(context);
        mLoadMoreContainerView = (ViewGroup) onInitLoadMoreView(loadMoreLayout);
        checkViewAttached(refreshLayout, loadMoreLayout);
        mViewHolderTracker = new HTViewHolderTracker();
    }

    private void checkViewAttached(FrameLayout refreshLayout, LinearLayout loadMoreLayout) {

        if (mRefreshContainerView != null) {
            if (refreshLayout != mRefreshContainerView) {
                throw new IllegalArgumentException("the refresh view has not been attached to parent view !");
            } else {
                mRefreshView = mRefreshContainerView.getChildAt(0);
            }
        }
        if (mLoadMoreContainerView != null) {
            if (mLoadMoreContainerView != loadMoreLayout) {
                throw new IllegalArgumentException("the loadMore view has not been attached to parent view !");
            } else {
                mLoadMoreView = mLoadMoreContainerView.getChildAt(0);
            }
        }
    }


    protected void onAttachedToRecyclerView() {

    }

    void setRecyclerView(HTBaseRecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        onAttachedToRecyclerView();
    }

    protected void updateViewSize() {
        if (mRecyclerView != null) {
            mRecyclerView.computeViewSize(mLoadMoreContainerView);
        }
    }

    /**
     * 设置刷新视图的背景色
     *
     * @param refreshViewBackgroundResId 背景色资源Id
     */
    public void setRefreshViewBackgroundResId(int refreshViewBackgroundResId) {
        if (refreshViewBackgroundResId > 0) {
            mRefreshViewBackgroundResId = refreshViewBackgroundResId;
        }
    }

    HTViewHolderTracker getViewHolderTracker() {
        return mViewHolderTracker;
    }

    void resetViewHolder() {
        mRecyclerView = null;
        mContext = null;
        mViewHolderTracker = null;
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
     * 自定义的刷新视图添加到parent中
     */
    public abstract View onInitRefreshView(ViewGroup parent);

    /**
     * 自定义的加载更多视图添加到parent中
     */
    public abstract View onInitLoadMoreView(ViewGroup parent);

    public final int getAnimationTime() {
        return mAnimationTime;
    }

    public final int getRefreshViewBackgroundResId() {
        return mRefreshViewBackgroundResId;
    }

    public final ViewGroup getRefreshContainerView() {
        return mRefreshContainerView;
    }

    public final ViewGroup getLoadMoreContainerView() {
        return mLoadMoreContainerView;
    }
}
