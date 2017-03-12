/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.graphics.PointF;

/**
 * 记录刷新头部视图在触发刷新过程中的相关位置信息
 * Created by fgx on 2017/2/8.
 */

public final class HTViewHolderTracker {

    private static final float PULL_DISTANCE_SCALE = 1.8f;
    private static final float SPRING_DISTANCE_SCALE = 1.0f;
    public final static int POSITION_IDLE = 0;

    private float mPullDistanceScale = PULL_DISTANCE_SCALE;
    private float mSpringDistanceScale = SPRING_DISTANCE_SCALE;

    private boolean mUnderTouch;
    private int mOrientation = HTOrientation.VERTICAL_DOWN;
    private final PointF mPtLastMove = new PointF();

    private float mRealOffsetXY;
    private float mOffsetX;
    private float mOffsetY;
    private int mRefreshViewSize;
    /**刷新视图移动上一次的位置*/
    private int mLastPos = 0;
    /**刷新视图移动当前的位置*/
    private int mCurrentPos = 0;
    /**按下时刷新视图的位置*/
    private int mPressedPos = 0;
    /**触发刷新事件的位置*/
    private int mOffsetToRefresh = 0;
    /**记录刷新结束那一刻的位置*/
    private int mRefreshCompletePos = 0;


    void setPullDistanceScale(float pullDistanceScale) {
        mPullDistanceScale = pullDistanceScale;
    }

    void setSpringDistanceScale(float springDistanceScale) {
        mSpringDistanceScale = springDistanceScale;
        mOffsetToRefresh = (int) (mRefreshViewSize * springDistanceScale);
    }

    /**手指移动时，计算位置信息*/
    private void processOnMove(float currentX, float currentY) {
        float offsetX = currentX - mPtLastMove.x;
        float offsetY = currentY - mPtLastMove.y;
        switch (mOrientation) {
            case HTOrientation.VERTICAL_DOWN:
                mRealOffsetXY = offsetY;
                setOffset(offsetX, offsetY / mPullDistanceScale);
                break;
            case HTOrientation.VERTICAL_UP:
                mRealOffsetXY = offsetY;
                setOffset(offsetX, offsetY / mPullDistanceScale);
                break;
            case HTOrientation.HORIZONTAL_RIGHT:
                mRealOffsetXY = offsetX;
                setOffset(offsetX / mPullDistanceScale, offsetY);
                break;
            case HTOrientation.HORIZONTAL_LEFT:
                mRealOffsetXY = offsetX;
                setOffset(offsetX / mPullDistanceScale, offsetY);
                break;
        }

    }


    public void onUIRefreshComplete() {
        mRefreshCompletePos = mCurrentPos;
    }

    /**超过刷新视图结束时的位置，可以再次触发刷新*/
    public boolean isOverCompletePos() {
        return mCurrentPos >= mRefreshCompletePos;
    }

    public void onRelease() {
        mUnderTouch = false;
    }

    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }

    public void onPressDown(float x, float y) {
        mUnderTouch = true;
        mPressedPos = mCurrentPos;
        mPtLastMove.set(x, y);
    }

    public final void setCurrentPos(int current) {
        mLastPos = mCurrentPos;
        mCurrentPos = current;
    }

    public final void onMove(float x, float y) {
        processOnMove(x, y);
        mPtLastMove.set(x, y);
    }

    private void setOffset(float x, float y) {
        mOffsetX = x;
        mOffsetY = y;
    }

    public float getOffsetX() {
        if (mOrientation == HTOrientation.HORIZONTAL_RIGHT) {
            return mOffsetX;
        } else {
            return -mOffsetX;
        }
    }

    public float getOffsetY() {
        if (mOrientation == HTOrientation.VERTICAL_DOWN) {
            return mOffsetY;
        } else {
            return -mOffsetY;
        }
    }

    public int getLastPos() {
        return mLastPos;
    }

    public int getCurrentPos() {
        return mCurrentPos;
    }

    public int getRefreshViewSize() {
        return mRefreshViewSize;
    }

    public void setRefreshViewSize(int refreshViewSize) {
        mRefreshViewSize = refreshViewSize;
        updateRefreshOffset();
    }

    private void updateRefreshOffset() {
        mOffsetToRefresh = (int) (mSpringDistanceScale * mRefreshViewSize);
    }


    public boolean isIdlePosition() {
        return mCurrentPos == POSITION_IDLE;
    }

    public boolean hasLeftIdlePosition() {
        return mCurrentPos > POSITION_IDLE;
    }

    /**是否刚离开初始位置*/
    public boolean hasJustLeftIdlePosition() {
        return mLastPos == POSITION_IDLE && hasLeftIdlePosition();
    }
    /**是否刚回到初始位置*/
    public boolean hasJustBackToIdlePosition() {
        return mLastPos != POSITION_IDLE && isIdlePosition();
    }

    /**是否打到触发刷新的位置*/
    public boolean isOverOffsetToRefresh() {
        return mCurrentPos >= mOffsetToRefresh;
    }

    public boolean hasMovedAfterPressedDown() {
        return mCurrentPos != mPressedPos;
    }

    public boolean hasJustReachedRefreshSizeFromIdle() {
        return mLastPos < mRefreshViewSize && mCurrentPos >= mRefreshViewSize;
    }

    public boolean isOverRefreshViewSize() {
        return mCurrentPos >= mRefreshViewSize;
    }

    public boolean isSamePos(int to) {
        return mCurrentPos == to;
    }

    public float getCurrentPercent() {
        return Math.min(mOffsetToRefresh == 0 ? 0 : mCurrentPos * 1f / mOffsetToRefresh, 1.0f);
    }

    public boolean isScrollOver(int to) {
        return to < POSITION_IDLE;
    }

    public boolean isUnderTouch() {
        return mUnderTouch;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public float getRealOffsetXY() {
        return mRealOffsetXY;
    }
}
