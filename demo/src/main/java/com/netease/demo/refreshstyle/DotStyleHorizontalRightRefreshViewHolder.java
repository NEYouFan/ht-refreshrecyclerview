package com.netease.demo.refreshstyle;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.netease.demo.R;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseRecyclerView;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseViewHolder;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTViewHolderTracker;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by stone on 16/3/3.
 */
public class DotStyleHorizontalRightRefreshViewHolder extends HTBaseViewHolder {
    protected View vLoadMore;
    protected View vNoMore;
    ObjectAnimator animator;
    private ViewGroup mRefreshLoadView;
    private ProgressBar mLoadMoreProgressBar;
    private int maxXValue;

    public DotStyleHorizontalRightRefreshViewHolder(Context context) {
        super(context);
        maxXValue = (int) ((mContext.getResources().getDimensionPixelSize(R.dimen.refresh_size) - mContext.getResources().getDimensionPixelSize(R.dimen.refresh_dot_size)) * 0.5);
        setLoadMoreViewBackgroundResId(android.R.color.holo_blue_light);
        setRefreshViewBackgroundResId(android.R.color.holo_blue_light);
        setPullDistanceScale(2.0f);
        setSpringDistanceScale(1.0f);
//        setAnimationTime(5000);
    }

    @Override
    public View onInitRefreshView() {
        View refreshView = View.inflate(mContext, R.layout.horizontal_refresh_view, null);
        mRefreshLoadView = (ViewGroup) refreshView.findViewById(R.id.refresh);
        return refreshView;
    }

    @Override
    public View onInitLoadMoreView() {
        View loadMoreView = View.inflate(mContext, R.layout.horizontal_load_more_view, null);
        mLoadMoreProgressBar = (ProgressBar) loadMoreView.findViewById(R.id.load_progress);
        vLoadMore = loadMoreView.findViewById(R.id.liner_loading);
        vNoMore = loadMoreView.findViewById(R.id.liner_no_more);

        return loadMoreView;
    }

    @Override
    public void onReset() {
        mRefreshLoadView.setRotation(0);
        mLoadMoreProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefreshPrepare() {

    }

    @Override
    public void onRefreshing() {
        mRefreshLoadView.setPivotX(0.5f * mRefreshLoadView.getMeasuredWidth());
        mRefreshLoadView.setPivotY(0.5f * mRefreshLoadView.getMeasuredHeight());
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(mRefreshLoadView, "rotation", 0.0f, 360.0f);
            animator.setDuration(1000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
        }
        if (animator.isRunning()) animator.cancel();
        animator.start();
    }

    @Override
    public void onRefreshComplete() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    @Override
    public void onRefreshPositionChange(float scale, float moveXDistance, int refreshStatus, HTViewHolderTracker viewHolderTracker) {
        if (refreshStatus != HTBaseRecyclerView.RefreshStatus.REFRESH_PREPARE) return;
        float diffX = moveXDistance + mRefreshLoadView.getX() - mRefreshView.getMeasuredWidth();
        if (diffX > 0) {
            diffX = diffX >= maxXValue ? maxXValue : diffX;
            ViewCompat.setTranslationY(mRefreshLoadView.getChildAt(0), -diffX);
            ViewCompat.setTranslationY(mRefreshLoadView.getChildAt(1), diffX);
        } else {
            ViewCompat.setTranslationY(mRefreshLoadView.getChildAt(0), 0);
            ViewCompat.setTranslationY(mRefreshLoadView.getChildAt(1), 0);
        }
    }

    @Override
    public void onLoadMoreStart(boolean hasMore) {
    }

    @Override
    public void onLoadMoreComplete(boolean hasMore) {
        if (hasMore) {
            vLoadMore.setVisibility(View.VISIBLE);
            vNoMore.setVisibility(View.GONE);

        } else {
            vLoadMore.setVisibility(View.GONE);
            vNoMore.setVisibility(View.VISIBLE);
        }
    }

}
