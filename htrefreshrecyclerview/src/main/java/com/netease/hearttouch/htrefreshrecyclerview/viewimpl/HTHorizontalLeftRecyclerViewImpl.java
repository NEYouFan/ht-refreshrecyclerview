/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.viewimpl;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 水平方向刷新实现
 */
public class HTHorizontalLeftRecyclerViewImpl extends HTHorizontalRecyclerViewImpl {

    public HTHorizontalLeftRecyclerViewImpl(Context context) {
        super(context);
    }

    public HTHorizontalLeftRecyclerViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HTHorizontalLeftRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public Boolean handleMoveAction(MotionEvent event) {
        float offsetX = mHTViewHolderTracker.getRealOffsetXY();
        float offsetY = mHTViewHolderTracker.getOffsetY();

        //如果X轴方向移动
        if ((Math.abs(offsetY) > mTouchSlop && Math.abs(offsetY) > Math.abs(offsetX))) {
            if (mHTViewHolderTracker.isIdlePosition()) {
                return defaultDispatchTouchEvent(event);
            }
        }
        boolean moveRight = offsetX > 0;
        boolean moveLeft = offsetX < 0;
        boolean canMoveRight = mHTViewHolderTracker.hasLeftIdlePosition();

        if (moveLeft && checkChildScroll()) {
            return defaultDispatchTouchEvent(event);
        }
        if (((moveRight && canMoveRight) || moveLeft) && shouldHandleRefresh()) {
            updatePos(mHTViewHolderTracker.getOffsetX());//传递PULL_DISTANCE_SCALE计算之后的offset
            return true;
        }
        return null;
    }

    public void performUpdateViewPosition(int offset) {
        ViewCompat.offsetLeftAndRight(mRefreshContainerView, -offset);
        ViewCompat.offsetLeftAndRight(mRecyclerView, -offset);
        invalidate();
    }

}
