/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.htrefreshrecyclerview.viewimpl;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseRecyclerView;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTWrapperAdapter;
import com.netease.hearttouch.htrefreshrecyclerview.utils.Utils;

import java.util.Arrays;

/**
 * 实现的一种刷新基类
 */
abstract class HTBaseRecyclerViewImpl extends HTBaseRecyclerView {

    private boolean isOver;
    private int mStartPosition;
    private int mCount;
    protected ValueAnimator mLoadMoreAnimator;
    protected ValueAnimator mRefreshAnimator;

    public HTBaseRecyclerViewImpl(Context context) {
        this(context, null);
    }

    public HTBaseRecyclerViewImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HTBaseRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean shouldHandleRefresh() {
        if (mHTViewHolder == null || mHTViewHolder.getRefreshView() == null || mRefreshDelegate == null) {
            return false;
        }
        if (mLoadMoreStatus == LoadMoreStatus.LOADING || mRefreshStatus == RefreshStatus.REFRESHING) {
            return false;
        }
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
            if (manager == null) {
                return false;
            }
            if (manager.getItemCount() <= 1 || Utils.getFirstItemPosition(manager, true) == 0) {//item数目为0或者只有添加的加载更多,允许
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldHandleLoadMore() {
        if (mHTViewHolder == null || mHTViewHolder.getLoadMoreView() == null || mLoadMoreDelegate == null ||
                mHTWrapperAdapter == null || !mHTWrapperAdapter.hasLoadMoreView()) {
            return false;
        }

        if (mLoadMoreStatus == LoadMoreStatus.LOADING || mRefreshStatus != RefreshStatus.IDLE) {
            return false;
        }

        if (!hasMore) {//没有更多数据不再触发加载更多
            if (isOver && !loadMoreShow) {//允许自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeLoadMoreViewPositionWithAnimation(-getLoadMoreSize(), null);
                    }
                }, 200);
            }
            return false;
        }

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();

        if (manager == null) {
            return false;
        }

        if (manager.getItemCount() <= 1) {//当前没有内容时,也允许触发加载更多
            return true;
        }

        int firstVisibleItem = Utils.getFirstItemPosition(manager, false);
        return firstVisibleItem + manager.getChildCount() >= manager.getItemCount();
    }


    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        mHTWrapperAdapter.setLoadMoreViewHolderListener(new HTWrapperAdapter.LoadMoreViewHolderListener() {
            @Override
            public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
                stopLoadMorePositionAnimation();
                //在拉动过程中,会触发该事件,这里做一些过滤,减少计算
                if (mRefreshStatus == RefreshStatus.IDLE || mRefreshStatus == RefreshStatus.REFRESHING) {
                    isOver = isCurrentItemSizeOver(false);
                }
                hideLoadMoreView(!isOver);
                mStartPosition = 0;
                mCount = 0;
            }

            @Override
            public void onBindData(RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            }

            @Override
            public void onItemRemoved(int positionStart, int itemCount) {
                mStartPosition = positionStart;//处理动画删除item时的loadMore的显示问题
                mCount = itemCount;
            }
        });
    }

    @Override
    protected void startRefresh() {
        if (mRefreshStatus != RefreshStatus.REFRESHING && mRefreshDelegate != null && mHTViewHolder != null) {
            mRefreshStatus = RefreshStatus.REFRESHING;
            processRefreshStatusChanged();
            mRefreshDelegate.onRefresh();
            //将刷新控件显示到正确的位置
            changeRefreshViewPositionWithAnimation(0, null);

        }
    }

    @Override
    protected void startLoadMore() {
        if (mLoadMoreStatus != LoadMoreStatus.LOADING && mLoadMoreDelegate != null && mHTViewHolder != null) {
            if (hasMore) {
                mLoadMoreStatus = LoadMoreStatus.LOADING;
                processLoadMoreStatusChanged();
                mLoadMoreDelegate.onLoadMore();
                showLoadingMoreView();
            }
        }
    }


    @Override
    protected void endRefresh() {
        if (mRefreshStatus == RefreshStatus.REFRESHING && mRefreshDelegate != null && mHTViewHolder != null) {
            mRefreshUIChangeListener.onRefreshComplete();
            changeRefreshViewPositionWithAnimation(mMinRefreshViewPadding, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mRefreshStatus = RefreshStatus.IDLE;
                    processRefreshStatusChanged();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }


    @Override
    protected void endLoadMore() {
        if (mLoadMoreStatus == LoadMoreStatus.LOADING && mLoadMoreDelegate != null && mHTViewHolder != null) {
            mLoadMoreStatus = LoadMoreStatus.IDLE;
            processLoadMoreStatusChanged();
            if (loadMoreShow) {//一直显示没有更多提示
                if (isOver) {
                    if (hasMore) {//还有更多数据的时候,满一屏动画隐藏,否则直接隐藏
                        changeLoadMoreViewPositionWithAnimation(-getLoadMoreSize(), null);
                    } else {
                        hideLoadMoreView(false);
                    }
                } else {
                    hideLoadMoreView(true);
                }
            } else {
                if (isOver) {
                    changeLoadMoreViewPositionWithAnimation(-getLoadMoreSize(), null);
                } else {
                    hideLoadMoreView(true);
                }
            }

        }

    }


    protected void stopRefreshPositionAnimation() {
        if (mRefreshAnimator != null && mRefreshAnimator.isRunning()) {
            mRefreshAnimator.cancel();
        }
    }

    protected void stopLoadMorePositionAnimation() {
        if (mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
            mLoadMoreAnimator.cancel();
        }
    }

    @Override
    public void setRefreshCompleted(boolean hasMore) {
        this.hasMore = hasMore;
        if (mLoadMoreStatus == LoadMoreStatus.IDLE) {//非加载更多数据时,数据变化需要重置加载更多视图
            processLoadMoreStatusChanged();
        }
        endLoadMore();
        endRefresh();

    }

    @Override
    public void startAutoRefresh() {
        if (mRecyclerView != null && mHTViewHolder != null && mHTViewHolder.getRefreshView() != null) {
            if (mRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreStatus != LoadMoreStatus.IDLE || mRefreshDelegate == null) {
                return;
            }
            if (mRefreshAnimator != null && mRefreshAnimator.isRunning()
                    || mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
                return;
            }
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.getItemCount() > 0) {
                layoutManager.scrollToPosition(0);
                mRefreshUIChangeListener.onRefreshPositionChange(1.0f, mMaxRefreshViewPadding);
                startRefresh();
            }
        }
    }

    @Override
    public void startAutoLoadMore() {
        if (mRecyclerView != null && mHTViewHolder != null && mHTWrapperAdapter != null && mHTWrapperAdapter.hasLoadMoreView()) {
            if (mRefreshStatus != RefreshStatus.IDLE || mLoadMoreStatus == LoadMoreStatus.LOADING) {
                return;
            }
            if (mRefreshAnimator != null && mRefreshAnimator.isRunning()
                    || mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
                return;
            }
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.getItemCount() > 0) {
                layoutManager.smoothScrollToPosition(mRecyclerView, null, layoutManager.getItemCount() - 1);
                startLoadMore();
            }
        }
    }

    private void showLoadingMoreView() {
        if (!isOver) {
            hideLoadMoreView(false);
        }
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.getItemCount() > 0) {
                layoutManager.smoothScrollToPosition(mRecyclerView, null, layoutManager.getItemCount() - 1);
            }
        }
    }


    private int calculateChildrenSize(boolean includeLoadMore) {
        RecyclerView.LayoutManager layoutManager;
        if (mRecyclerView == null || (layoutManager = mRecyclerView.getLayoutManager()) == null)
            return 0;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) return 0;
        int columns = 1;//列数
        int rows;//行数
        if (layoutManager instanceof GridLayoutManager) {
            columns = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            columns = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        rows = childCount / columns + (childCount % columns == 0 ? 0 : 1);//计算行数
        int[] columnSizes = new int[columns];
        Arrays.fill(columnSizes, 0);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j;
                View child = layoutManager.getChildAt(index);
                if (child != null && (includeLoadMore || child != mLoadMoreContainerView)) {
                    int position = layoutManager.getPosition(child);
                    if (position > -1 && position >= mStartPosition && position < mStartPosition + mCount) {
                        continue;//删除item时,因为pre-layout的原因,需要排除被删除的view
                    }
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
                    columnSizes[j] += getOrientation() == VERTICAL ?
                            layoutManager.getDecoratedMeasuredHeight(child) + lp.bottomMargin + lp.topMargin :
                            layoutManager.getDecoratedMeasuredWidth(child) + lp.leftMargin + lp.rightMargin;
                }
            }
        }
        Arrays.sort(columnSizes);//找出高度或者宽度的最大值
        return columnSizes[0];
    }


    /** 在加载更多视图被attachToWindow之前,判断当前显示的内容是否达到显示加载更多的条件 */
    public boolean isCurrentItemSizeOver(boolean includeLoadMore) {
        int currentSize = getOrientation() == VERTICAL ? mRecyclerView.getMeasuredHeight() : mRecyclerView.getMeasuredWidth();
        boolean canScroll = calculateChildrenSize(includeLoadMore) >= currentSize;
        return mLoadMoreDelegate != null && mRefreshStatus != HTBaseRecyclerView.RefreshStatus.REFRESHING && canScroll;
    }

    /** 隐藏刷新控件带动画 */
    protected void changeRefreshViewPositionWithAnimation(int targetPosition, @Nullable Animator.AnimatorListener animatorListener) {
        int startValue = 0;
        switch (mHTOrientation) {
            case Orientation.VERTICAL_DOWN:
                startValue = mRefreshContainerView.getPaddingTop();
                break;
            case Orientation.VERTICAL_UP:
                startValue = mRefreshContainerView.getPaddingBottom();
                break;
            case Orientation.HORIZONTAL_LEFT:
                startValue = mRefreshContainerView.getPaddingRight();
                break;
            case Orientation.HORIZONTAL_RIGHT:
                startValue = mRefreshContainerView.getPaddingLeft();
                break;
        }
        if (startValue < mMinRefreshViewPadding) return;
        mRefreshAnimator = ValueAnimator.ofInt(startValue, targetPosition);
        mRefreshAnimator.setDuration(mHTViewHolder.getAnimationTime());
        mRefreshAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int padding = (int) animation.getAnimatedValue();
                mRefreshContainerView.setPadding(mHTOrientation == Orientation.HORIZONTAL_RIGHT ? padding : 0,
                        mHTOrientation == Orientation.VERTICAL_DOWN ? padding : 0,
                        mHTOrientation == Orientation.HORIZONTAL_LEFT ? padding : 0,
                        mHTOrientation == Orientation.VERTICAL_UP ? padding : 0);
            }
        });
        if (animatorListener != null) {
            mRefreshAnimator.addListener(animatorListener);
        }
        mRefreshAnimator.start();
    }


    /** 隐藏刷新空间带动画 */
    protected void changeLoadMoreViewPositionWithAnimation(int targetPosition, @Nullable Animator.AnimatorListener animatorListener) {
        int startValue = 0;
        switch (mHTOrientation) {
            case Orientation.VERTICAL_DOWN:
                startValue = mLoadMoreContainerView.getPaddingBottom();
                break;
            case Orientation.VERTICAL_UP:
                startValue = mLoadMoreContainerView.getPaddingTop();
                break;
            case Orientation.HORIZONTAL_LEFT:
                startValue = mLoadMoreContainerView.getPaddingLeft();
                break;
            case Orientation.HORIZONTAL_RIGHT:
                startValue = mLoadMoreContainerView.getPaddingRight();
                break;
        }
        if (startValue <= -getLoadMoreSize()) return;
        mLoadMoreAnimator = ValueAnimator.ofInt(startValue, targetPosition);
        mLoadMoreAnimator.setDuration(mHTViewHolder.getAnimationTime());
        mLoadMoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int padding = (int) animation.getAnimatedValue();

                mLoadMoreContainerView.setPadding(
                        mHTOrientation == Orientation.HORIZONTAL_LEFT ? padding : 0,
                        mHTOrientation == Orientation.VERTICAL_UP ? padding : 0,
                        mHTOrientation == Orientation.HORIZONTAL_RIGHT ? padding : 0,
                        mHTOrientation == Orientation.VERTICAL_DOWN ? padding : 0);
            }
        });
        if (animatorListener != null) {
            mLoadMoreAnimator.addListener(animatorListener);
        }
        mLoadMoreAnimator.start();
    }

}

