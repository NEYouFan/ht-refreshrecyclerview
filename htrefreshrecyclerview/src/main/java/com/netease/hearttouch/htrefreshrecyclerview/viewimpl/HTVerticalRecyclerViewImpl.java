/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.viewimpl;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 垂直方向刷新的实现
 */
public class HTVerticalRecyclerViewImpl extends HTBaseRecyclerViewImpl {

    public HTVerticalRecyclerViewImpl(Context context) {
        this(context, null);
    }

    public HTVerticalRecyclerViewImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HTVerticalRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean handleUpOrCancelAction(MotionEvent event) {

        int currentPadding = mHTOrientation == Orientation.VERTICAL_DOWN ? mRefreshContainerView.getPaddingTop() : mRefreshContainerView.getPaddingBottom();
        // 如果当前头部刷新控件没有完全隐藏，则需要返回true，自己消耗ACTION_UP事件
        boolean isReturnTrue = currentPadding != mMinRefreshViewPadding;
        if (mRefreshStatus == RefreshStatus.PULL_DOWN || mRefreshStatus == RefreshStatus.IDLE) {
            // 处于下拉刷新状态，松手时隐藏下拉刷新控件
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
        } else if (mRefreshStatus == RefreshStatus.RELEASE_TO_REFRESH) {
            // 处于松开进入刷新状态，松手时完全显示下拉刷新控件，进入正在刷新状态
            startRefresh();
        }

        mRefreshDownY = -1;
        return isReturnTrue;
    }

    @Override
    public boolean handleMoveAction(MotionEvent event) {
        if (mRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreStatus == LoadMoreStatus.LOADING) {
            return false;
        }
        stopRefreshPositionAnimation();
        if (mRefreshDownY == -1) {
            mRefreshDownY = event.getY();
        }
        int currentPadding;
        float diffY;
        if (shouldHandleRefresh()) {
            if (mHTOrientation == Orientation.VERTICAL_DOWN) {
                currentPadding = mRefreshContainerView.getPaddingTop();
                diffY = event.getY() - mRefreshDownY;
            } else {
                currentPadding = mRefreshContainerView.getPaddingBottom();
                diffY = mRefreshDownY - event.getY();
            }
            diffY = diffY / mHTViewHolder.getPullDistanceScale();
            int paddingTopOrBottom = (int) (currentPadding + diffY);
            paddingTopOrBottom = Math.max(paddingTopOrBottom, mMinRefreshViewPadding);
            // 下拉刷新控件完全显示，并且当前状态没有处于释放开始刷新状态
            if (paddingTopOrBottom > 0 && mRefreshStatus != RefreshStatus.RELEASE_TO_REFRESH) {
                mRefreshStatus = RefreshStatus.RELEASE_TO_REFRESH;
                processRefreshStatusChanged();
                mRefreshUIChangeListener.onRefreshPositionChange(1.0f, paddingTopOrBottom + Math.abs(mMinRefreshViewPadding));
            } else if (paddingTopOrBottom < 0) { //下拉刷新控件没有完全显示
                if (mRefreshStatus != RefreshStatus.PULL_DOWN) {  //并且当前状态没有处于下拉刷新状态
                    boolean isPreviousIdle = mRefreshStatus == RefreshStatus.IDLE;
                    mRefreshStatus = RefreshStatus.PULL_DOWN;
                    mRefreshUIChangeListener.onRefreshStart(isPreviousIdle);

                }
                //计算下拉距离比值
                float scale = 1 - paddingTopOrBottom * 1.0f / (mMinRefreshViewPadding == 0 ? 1 : mMinRefreshViewPadding);
                mRefreshUIChangeListener.onRefreshPositionChange(scale, paddingTopOrBottom + Math.abs(mMinRefreshViewPadding));
            }
            paddingTopOrBottom = Math.min(paddingTopOrBottom, mMaxRefreshViewPadding);
            if (mHTOrientation == Orientation.VERTICAL_DOWN) {
                mRefreshContainerView.setPadding(0, paddingTopOrBottom, 0, 0);
            } else {
                mRefreshContainerView.setPadding(0, 0, 0, paddingTopOrBottom);
            }
            mRefreshDownY = event.getY();
            return true;
        }
        return false;
    }

}
