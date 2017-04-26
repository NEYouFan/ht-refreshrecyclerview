/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseRecyclerView;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseViewHolder;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTRefreshRecyclerViewInterface;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTOrientation;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTHorizontalLeftRecyclerViewImpl;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTHorizontalRightRecyclerViewImpl;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTVerticalDownRecyclerViewImpl;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTVerticalUpRecyclerViewImpl;

/**
 * 用户真正需要使用的控件对象,内部采用代理模式
 */
public class HTRefreshRecyclerView extends FrameLayout implements HTRefreshRecyclerViewInterface {

    /**
     * 刷新控件实现代理对象,根据用户配置生成
     */
    private final HTRefreshRecyclerViewInterface mRecyclerViewProxy;

    public HTRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public HTRefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HTRefreshRecyclerView(Context context, final AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        removeAllViews();
        getOrientation(attrs);
        switch (getOrientation(attrs)) {//根据xml配置信息生成代理的刷新控件对象
            case HTOrientation.HORIZONTAL_LEFT:
                mRecyclerViewProxy = new HTHorizontalLeftRecyclerViewImpl(context, attrs, defStyleAttr);
                break;
            case HTOrientation.HORIZONTAL_RIGHT:
                mRecyclerViewProxy = new HTHorizontalRightRecyclerViewImpl(context, attrs, defStyleAttr);
                break;
            case HTOrientation.VERTICAL_UP:
                mRecyclerViewProxy = new HTVerticalUpRecyclerViewImpl(context, attrs, defStyleAttr);
                break;
            case HTOrientation.VERTICAL_DOWN:
                mRecyclerViewProxy = new HTVerticalDownRecyclerViewImpl(context, attrs, defStyleAttr);
                break;
            default:
                mRecyclerViewProxy = new HTVerticalDownRecyclerViewImpl(context, attrs, defStyleAttr);
                break;

        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView((HTBaseRecyclerView) mRecyclerViewProxy, layoutParams);
    }

    private int getOrientation(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HTRefreshRecyclerView);
        try {
            return typedArray.getInteger(R.styleable.HTRefreshRecyclerView_htOrientation, 1);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * 设置全局的刷新样式
     */
    public static void setRefreshViewHolderClass(@NonNull Class<? extends HTBaseViewHolder> mViewHolderClass) {
        HTBaseRecyclerView.setRefreshViewHolderClass(mViewHolderClass);
    }

    @Override
    public void setRefreshCompleted(boolean hasMore) {
        mRecyclerViewProxy.setRefreshCompleted(hasMore);
    }

    @Override
    public void startAutoRefresh() {
        mRecyclerViewProxy.startAutoRefresh();
    }

    @Override
    public void startAutoLoadMore() {
        mRecyclerViewProxy.startAutoLoadMore();
    }

    public void setLoadMoreViewShow(boolean loadMoreShow) {
        mRecyclerViewProxy.setLoadMoreViewShow(loadMoreShow);
    }

    public void setEnableScrollOnRefresh(boolean enableScrollOnReFresh) {
        mRecyclerViewProxy.setEnableScrollOnRefresh(enableScrollOnReFresh);
    }

    @Override
    public void setOnLoadMoreListener(HTLoadMoreListener loadMoreDelegate) {
        mRecyclerViewProxy.setOnLoadMoreListener(loadMoreDelegate);
    }

    @Override
    public void setOnRefreshListener(HTRefreshListener refreshDelegate) {
        mRecyclerViewProxy.setOnRefreshListener(refreshDelegate);
    }

    @Override
    public void setRefreshViewHolder(@NonNull HTBaseViewHolder refreshViewHolder) {
        mRecyclerViewProxy.setRefreshViewHolder(refreshViewHolder);
    }

    @Override
    public void removeOnScrollListener(HTBaseRecyclerView.OnScrollListener listener) {
        mRecyclerViewProxy.removeOnScrollListener(listener);
    }

    @Override
    public void addOnScrollListener(HTBaseRecyclerView.OnScrollListener listener) {
        mRecyclerViewProxy.addOnScrollListener(listener);
    }

    @Override
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerViewProxy.removeOnItemTouchListener(listener);
    }

    @Override
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerViewProxy.addOnItemTouchListener(listener);
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View child) {
        return mRecyclerViewProxy.getChildViewHolder(child);
    }

    @Override
    public void clearOnScrollListeners() {
        mRecyclerViewProxy.clearOnScrollListeners();
    }

    @Override
    public void setRecyclerListener(RecyclerView.RecyclerListener listener) {
        mRecyclerViewProxy.setRecyclerListener(listener);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mRecyclerViewProxy.getAdapter();
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerViewProxy.setAdapter(adapter);
    }

    @Override
    public boolean hasFixedSize() {
        return mRecyclerViewProxy.hasFixedSize();
    }

    @Override
    public void setHasFixedSize(boolean hasFixedSize) {
        mRecyclerViewProxy.setHasFixedSize(hasFixedSize);
    }

    @Override
    public void removeItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecyclerViewProxy.removeItemDecoration(decor);
    }

    @Override
    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecyclerViewProxy.addItemDecoration(decor);
    }

    @Override
    public void addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        mRecyclerViewProxy.addItemDecoration(decor, index);
    }

    @Override
    public RecyclerView.ItemAnimator getItemAnimator() {
        return mRecyclerViewProxy.getItemAnimator();
    }

    @Override
    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerViewProxy.setItemAnimator(animator);
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerViewProxy.getLayoutManager();
    }

    @Override
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecyclerViewProxy.setLayoutManager(layoutManager);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecyclerViewProxy.getRecyclerView();
    }

    @Override
    public void setRecyclerViewDragListener(HTRecyclerViewDragListener recyclerViewDragListener) {
        mRecyclerViewProxy.setRecyclerViewDragListener(recyclerViewDragListener);
    }
}
