/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.viewimpl;

import android.content.Context;
import android.util.AttributeSet;


public abstract class HTHorizontalRecyclerViewImpl extends HTBaseRecyclerViewImpl {

    public HTHorizontalRecyclerViewImpl(Context context) {
        this(context, null);

    }

    public HTHorizontalRecyclerViewImpl(Context context, AttributeSet attrs) {
        this(context, attrs,0);

    }

    public HTHorizontalRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollJob = new HorizontalRightScrollJob();
    }


    class HorizontalRightScrollJob extends ScrollJob {

        void handleScroll(boolean isFinish) {
            int curX = mScroller.getCurrX();
            int deltaY = curX - mLastFlingXY;
            if (!isFinish) {
                mLastFlingXY = curX;
                updatePos(deltaY);
                post(this);
            } else {
                finish();
            }
        }

        void tryToScrollTo(int to, int duration) {
            if (mHTViewHolderTracker.isSamePos(to)) {
                return;
            }
            mStartPos = mHTViewHolderTracker.getCurrentPos();
            mTargetPos = to;
            int distance = to - mStartPos;

            removeCallbacks(this);

            mLastFlingXY = 0;

            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.startScroll(0, 0, distance, 0, duration);
            post(this);
            mScrollRunning = true;
        }
    }
}
