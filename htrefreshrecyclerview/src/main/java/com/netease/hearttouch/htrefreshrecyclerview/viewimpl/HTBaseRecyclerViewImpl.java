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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseRecyclerView;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTOrientation;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTViewHolderTracker;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTWrapperAdapter;
import com.netease.hearttouch.htrefreshrecyclerview.utils.Utils;

import java.util.Arrays;
import java.util.List;

import static com.netease.hearttouch.htrefreshrecyclerview.utils.Utils.getFirstItemPosition;

/**
 * 实现的一种刷新基类
 */
abstract class HTBaseRecyclerViewImpl extends HTBaseRecyclerView {
    private boolean mScreenFilled;
    private int mStartPosition;
    private int mItemCount;
    protected ValueAnimator mLoadMoreAnimator;
    protected ScrollJob mScrollJob;
    protected MotionEvent mLastMoveEvent;
    protected boolean mHasSendCancelEvent;
    protected boolean mAutoRefresh;

    public HTBaseRecyclerViewImpl(Context context) {
        super(context);
    }

    public HTBaseRecyclerViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HTBaseRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean shouldHandleRefresh() {
        if (mHTViewHolder == null || mRefreshContainerView == null || mRefreshDelegate == null) {
            return false;
        }
        if (mRecyclerView.getLayoutManager() == null || mLoadMoreStatus != LoadMoreStatus.IDLE) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldHandleLoadMore() {
        if (mHTViewHolder == null || mLoadMoreContainerView == null || mLoadMoreDelegate == null ||
                mHTWrapperAdapter == null || !mHTWrapperAdapter.hasLoadMoreView()) {
            return false;
        }

        if (mLoadMoreStatus == LoadMoreStatus.LOADING || mRefreshStatus != RefreshStatus.IDLE) {
            return false;
        }

        if (!mHasMore) {//没有更多数据不再触发加载更多
            if (mScreenFilled && !mLoadMoreViewDisplay) {//允许自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeLoadMoreViewPositionWithAnimation(-mLoadMoreViewSize, null);
                    }
                }, mHTViewHolder.getAnimationTime());
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

        int firstVisibleItem = getFirstItemPosition(manager, false);//获取第一个可见的Item位置
        return firstVisibleItem + manager.getChildCount() >= manager.getItemCount();
    }


    public boolean defaultDispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled() || mRecyclerView == null || mRefreshContainerView == null) {
            return defaultDispatchTouchEvent(event);
        }

        if (mRefreshStatus == RefreshStatus.REFRESHING && !mEnableScrollOnReFresh) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHTViewHolderTracker.onRelease();
                if (mHTViewHolderTracker.hasLeftIdlePosition()) { //刷新视图不在初始位置
                    onViewRelease();//释放
                    if (mHTViewHolderTracker.hasMovedAfterPressedDown()) {
                        sendCancelEvent();//通知子view不在处理事件
                        return true;
                    }
                    return defaultDispatchTouchEvent(event);
                } else {
                    return defaultDispatchTouchEvent(event);
                }

            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                //记录按下的位置
                mHTViewHolderTracker.onPressDown(event.getX(), event.getY());
                //如果正在滚动，则立即终止
                mScrollJob.abortIfWorking();
                defaultDispatchTouchEvent(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = event; //记录事件，以方便模拟事件
                mHTViewHolderTracker.onMove(event.getX(), event.getY());
                Boolean handled = handleMoveAction(event);
                if (handled != null) return handled;
        }
        return defaultDispatchTouchEvent(event);
    }


    protected void onViewRelease() {
        tryToPerformRefresh();
        if (mRefreshStatus == RefreshStatus.REFRESHING) {
            if (mHTViewHolderTracker.isOverRefreshViewSize()) { //触发刷新后，需要保持刷新视图，将刷新视图滚动到对应的刷新位置
                mScrollJob.tryToScrollTo(mHTViewHolderTracker.getRefreshViewSize(), mHTViewHolder.getAnimationTime(), 0);
            }
        } else {
            if (mRefreshStatus == RefreshStatus.COMPLETE) {
                notifyRefreshComplete();
            } else {
                tryScrollBackToOriginal();
            }
        }
    }


    protected void tryToPerformRefresh() {
        if (mRefreshStatus != RefreshStatus.REFRESH_PREPARE) {
            return;
        }
        if ((mHTViewHolderTracker.isOverRefreshViewSize() && isAutoRefresh()) || mHTViewHolderTracker.isOverOffsetToRefresh()) {
            mRefreshStatus = RefreshStatus.REFRESHING;
            performRefresh();
        }
    }

    protected void performRefresh() {
        if (mRefreshUIChangeListener != null) {
            mRefreshUIChangeListener.onRefreshing();
            mRefreshDelegate.onRefresh();
        }
    }

    protected void tryScrollBackToOriginal() {
        if (!mHTViewHolderTracker.isUnderTouch()) {
            mScrollJob.tryToScrollTo(HTViewHolderTracker.POSITION_IDLE, mHTViewHolder.getAnimationTime(), 0);
        }
    }

    protected void notifyRefreshComplete() {
        mRefreshUIChangeListener.onRefreshComplete();
        mHTViewHolderTracker.onUIRefreshComplete();
        tryScrollBackToOriginal();
        reset();
    }


    protected boolean reset() {
        if ((mRefreshStatus == RefreshStatus.COMPLETE || mRefreshStatus == RefreshStatus.REFRESH_PREPARE) && mHTViewHolderTracker.isIdlePosition()) {
            mHTViewHolder.onReset();
            mRefreshStatus = RefreshStatus.IDLE;
            mAutoRefresh = false;
            return true;
        }
        return false;
    }

    protected void sendCancelEvent() {
        // The ScrollJob will update position and lead to send cancel event when mLastMoveEvent is null.
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        defaultDispatchTouchEvent(e);
    }

    protected void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        defaultDispatchTouchEvent(e);
    }

    protected void onViewHolderScrollFinish() {
        if (mHTViewHolderTracker.hasLeftIdlePosition() && isAutoRefresh()) {
            onViewRelease();
        }
    }


    protected void onViewHolderScrollAbort() {
        if (mHTViewHolderTracker.hasLeftIdlePosition() && isAutoRefresh()) {
            onViewRelease();
        }
    }

    public boolean isAutoRefresh() {
        return mAutoRefresh;
    }

    /**
     * 刷新视图位置移动处理
     *
     * @param offset 偏移量
     */
    protected void updatePos(float offset) {
        if ((offset < 0 && mHTViewHolderTracker.isIdlePosition())) {
            return;
        }
        int to = mHTViewHolderTracker.getCurrentPos() + (int) offset;
        if (mHTViewHolderTracker.isScrollOver(to)) {
            to = HTViewHolderTracker.POSITION_IDLE;
        }

        mHTViewHolderTracker.setCurrentPos(to);
        int change = to - mHTViewHolderTracker.getLastPos();
        if (change == 0) {
            return;
        }

        boolean isUnderTouch = mHTViewHolderTracker.isUnderTouch();
        if (isUnderTouch && !mHasSendCancelEvent && mHTViewHolderTracker.hasMovedAfterPressedDown()) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }

        if ((mHTViewHolderTracker.hasJustLeftIdlePosition() && mRefreshStatus == RefreshStatus.IDLE) /*||
                (mHTViewHolderTracker.isOverCompletePos() && mRefreshStatus == RefreshStatus.COMPLETE)*/) {
            mRefreshStatus = RefreshStatus.REFRESH_PREPARE;
            mRefreshUIChangeListener.onRefreshPrepare();
            if (mRecyclerViewDragListener != null) {
                mRecyclerViewDragListener.onRefreshViewPrepareToMove();
            }
        }

        // 回到初始位置
        if (mHTViewHolderTracker.hasJustBackToIdlePosition()) {
            reset();
            if (isUnderTouch) {
                sendDownEvent();
            }
        }
        if (mRefreshStatus == RefreshStatus.REFRESH_PREPARE) {
            //处理自动刷新
            if (mHTViewHolderTracker.hasJustReachedRefreshSizeFromIdle() && isAutoRefresh()) {
                tryToPerformRefresh();
            }
        }

        performUpdateViewPosition(change);//更新界面视图
        mRefreshUIChangeListener.onRefreshPositionChange(mHTViewHolderTracker.getCurrentPercent(), mHTViewHolderTracker.getCurrentPos(), mRefreshStatus, mHTViewHolderTracker);
    }

    protected abstract void performUpdateViewPosition(int change);

    @Override
    protected void initLoadMoreSupported() {
        mLoadMoreViewHolderListener = new HTWrapperAdapter.LoadMoreViewHolderListener() {
            @Override
            public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
                stopLoadMoreAnimation();
                mScreenFilled = isCurrentItemSizeOver(false);
                hideLoadMoreView(!mScreenFilled);
                mStartPosition = 0;
                mItemCount = 0;
            }

            @Override
            public void onBindData(RecyclerView.ViewHolder holder, int position) {
            }

            public void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

            }

            @Override
            public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            }

            @Override
            public void onItemRemoved(int positionStart, int itemCount) {
                mStartPosition = positionStart;//处理动画删除item时的loadMore的显示问题
                mItemCount = itemCount;
            }
        };
        if (mHTWrapperAdapter != null) {
            mHTWrapperAdapter.setLoadMoreViewHolderListener(mLoadMoreViewHolderListener);
        }
    }

    @Override
    protected void performLoadMore() {
        if (mLoadMoreStatus != LoadMoreStatus.LOADING && mLoadMoreDelegate != null && mHTViewHolder != null) {
            if (mHasMore) {
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
            mRefreshStatus = RefreshStatus.COMPLETE;
            if (mScrollJob.isScrollRunning() && isAutoRefresh()) {
                return;
            }
            notifyRefreshComplete();
        }
    }


    @Override
    protected void endLoadMore() {
        if (mLoadMoreStatus == LoadMoreStatus.LOADING && mLoadMoreDelegate != null && mHTViewHolder != null) {
            mLoadMoreStatus = LoadMoreStatus.IDLE;
            processLoadMoreStatusChanged();
            if (mLoadMoreViewDisplay) {//一直显示没有更多提示
                if (mScreenFilled) {
                    if (mHasMore) {//还有更多数据的时候,满一屏动画隐藏,否则直接隐藏
                        changeLoadMoreViewPositionWithAnimation(-mLoadMoreViewSize, null);
                    } else {
                        hideLoadMoreView(false);
                    }
                } else {
                    hideLoadMoreView(true);
                }
            } else {
                if (mScreenFilled) {
                    changeLoadMoreViewPositionWithAnimation(-mLoadMoreViewSize, null);
                } else {
                    hideLoadMoreView(true);
                }
            }

        }

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScrollJob != null) {
            mScrollJob.destroy();
        }
    }

    protected void stopLoadMoreAnimation() {
        if (mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
            mLoadMoreAnimator.cancel();
        }
    }

    @Override
    public void setRefreshCompleted(boolean hasMore) {
        this.mHasMore = hasMore;
        if (mLoadMoreStatus == LoadMoreStatus.IDLE) {//非加载更多数据时,数据变化需要重置加载更多视图
            processLoadMoreStatusChanged();
        }
        endLoadMore();
        endRefresh();

    }


    @Override
    public void startAutoRefresh() {
        if (mRecyclerView != null && mHTViewHolder != null && mHTViewHolder.getRefreshContainerView() != null) {
            if (mRefreshStatus != RefreshStatus.IDLE || mLoadMoreStatus != LoadMoreStatus.IDLE || mRefreshDelegate == null) {
                return;
            }
            if (mScrollJob.isScrollRunning() || mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
                return;
            }
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                int pos = Utils.getFirstItemPosition(layoutManager, true);
                layoutManager.scrollToPosition(0);
                mScrollJob.tryToScrollTo(mHTViewHolderTracker.getOffsetToRefresh(), mHTViewHolder.getAnimationTime(), 10 * pos);
                mAutoRefresh = true;
            }
        }
    }

    @Override
    public void startAutoLoadMore() {
        if (mRecyclerView != null && mHTViewHolder != null && mHTWrapperAdapter != null && mHTWrapperAdapter.hasLoadMoreView()) {
            if (mRefreshStatus != RefreshStatus.IDLE || mLoadMoreStatus != LoadMoreStatus.IDLE) {
                return;
            }
            if (mScrollJob.isScrollRunning() || mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
                return;
            }
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.getItemCount() > 0) {
                layoutManager.smoothScrollToPosition(mRecyclerView, null, layoutManager.getItemCount() - 1);
                performLoadMore();
            }
        }
    }

    private void showLoadingMoreView() {
        if (!mScreenFilled) {
            hideLoadMoreView(false);
        }
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.getItemCount() > 0) {
                layoutManager.smoothScrollToPosition(mRecyclerView, null, layoutManager.getItemCount() - 1);
            }
        }
    }


    private int calculateChildrenSize(boolean includeLoadMore) {// TODO: 2017/8/8 高度计算有问题 
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
                    if (position > -1 && position >= mStartPosition && position < mStartPosition + mItemCount) {
                        continue;//删除item时,因为pre-layout的原因,需要排除被删除的view
                    }
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
                    columnSizes[j] += checkOrientationVertical() ?
                            layoutManager.getDecoratedMeasuredHeight(child) + lp.bottomMargin + lp.topMargin :
                            layoutManager.getDecoratedMeasuredWidth(child) + lp.leftMargin + lp.rightMargin;
                }
            }
        }
        Arrays.sort(columnSizes);//找出高度或者宽度的最大值
        return columnSizes[0];
    }


    /**
     * 在加载更多视图被attachToWindow之前,判断当前显示的内容是否达到显示加载更多的条件
     */
    public boolean isCurrentItemSizeOver(boolean includeLoadMore) {
        int currentSize = checkOrientationVertical() ? mRecyclerView.getMeasuredHeight() : mRecyclerView.getMeasuredWidth();
        boolean canScroll = calculateChildrenSize(includeLoadMore) >= currentSize;
        return mLoadMoreDelegate != null && mRefreshStatus != HTBaseRecyclerView.RefreshStatus.REFRESHING && canScroll;
    }

    /**
     * 隐藏刷新空间带动画
     */
    protected void changeLoadMoreViewPositionWithAnimation(int targetPosition, @Nullable Animator.AnimatorListener animatorListener) {
        int startValue = 0;
        switch (mHTOrientation) {
            case HTOrientation.VERTICAL_DOWN:
                startValue = mLoadMoreContainerView.getPaddingBottom();
                break;
            case HTOrientation.VERTICAL_UP:
                startValue = mLoadMoreContainerView.getPaddingTop();
                break;
            case HTOrientation.HORIZONTAL_LEFT:
                startValue = mLoadMoreContainerView.getPaddingLeft();
                break;
            case HTOrientation.HORIZONTAL_RIGHT:
                startValue = mLoadMoreContainerView.getPaddingRight();
                break;
        }
        if (startValue <= -mLoadMoreViewSize) return;
        if (mLoadMoreAnimator != null && mLoadMoreAnimator.isRunning()) {
            mLoadMoreAnimator.end();
        }
        mLoadMoreAnimator = ValueAnimator.ofInt(startValue, targetPosition);
        mLoadMoreAnimator.setDuration(mHTViewHolder.getAnimationTime());
        mLoadMoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int padding = (int) animation.getAnimatedValue();
                mLoadMoreContainerView.setPadding(
                        mHTOrientation == HTOrientation.HORIZONTAL_LEFT ? padding : 0,
                        mHTOrientation == HTOrientation.VERTICAL_UP ? padding : 0,
                        mHTOrientation == HTOrientation.HORIZONTAL_RIGHT ? padding : 0,
                        mHTOrientation == HTOrientation.VERTICAL_DOWN ? padding : 0);
            }
        });
        if (animatorListener != null) {
            mLoadMoreAnimator.addListener(animatorListener);
        }
        post(new Runnable() {
            public void run() {
                mLoadMoreAnimator.start();
            }
        });
    }


    abstract class ScrollJob implements Runnable {

        int mLastFlingXY;
        Scroller mScroller;
        boolean mScrollRunning = false;
        int mStartPos;
        int mTargetPos;

        public ScrollJob() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            handleScroll(finish);
        }

        abstract void handleScroll(boolean isFinish);

        void finish() {
            reset();
            onViewHolderScrollFinish();
        }

        void reset() {
            mScrollRunning = false;
            mLastFlingXY = 0;
            removeCallbacks(this);
        }

        void destroy() {
            reset();
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        boolean isScrollRunning() {
            return mScrollRunning;
        }

        void abortIfWorking() {
            if (mScrollRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                onViewHolderScrollAbort();
                reset();
            }
        }

        abstract void tryToScrollTo(int to, int duration, long delayMillis);
    }

}

