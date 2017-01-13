/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.netease.hearttouch.htrefreshrecyclerview.HTLoadMoreListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRecyclerViewDragListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRefreshListener;

/**
 * 定义刷新控件的一些通用方法
 */
public interface HTRefreshRecyclerViewInterface {

    /**
     * 设置刷新完成,hasMore主要用于决定加载更多视图的显示
     * @param hasMore 是否还有更多数据
     */
    void setRefreshCompleted(boolean hasMore);

    /** 触发自动刷新,刷新状态的视图可见 */
    void startAutoRefresh();

    /** 触发自动加载更多,加载更多状态的视图可见 */
    void startAutoLoadMore();

    /**
     * 设置没有更多数据时,加载更多视图的显示样式,默认为一直显示
     * @param loadMoreShow false: 自动隐藏 true:一直显示
     */
    void setLoadMoreViewShow(boolean loadMoreShow);

    /**
     * 是否允许刷新的时候，界面滚动,默认不能滚动
     * @param enableScrollOnReFresh
     */
    void setEnableScrollOnRefresh(boolean enableScrollOnReFresh);

    /**
     * 设置加载更多事件监听,若设置为null,则不触发加载更多
     * @param loadMoreDelegate 加载更多事件监听对象
     */
    void setOnLoadMoreListener(HTLoadMoreListener loadMoreDelegate);

    /**
     * 设置刷新事件监听,若设置为null,则不触发刷新
     * @param refreshDelegate 刷新事件监听对象
     */
    void setOnRefreshListener(HTRefreshListener refreshDelegate);

    /**
     * 设置刷新方向的拖拽事件监听,包括拉动刷新操作和列表滚动操作
     * @param recyclerViewDragListener 拖拽事件监听对象
     */
    void setRecyclerViewDragListener(HTRecyclerViewDragListener recyclerViewDragListener);


    /** 返回返回控件内被包裹的真正RecyclerView ,建议在当前的刷新控件不能满足满足某些功能需求的情况下使用 */
    RecyclerView getRecyclerView();

    /**
     * 设置自定义的刷新视图样式,需要实现{@link HTBaseRecyclerView.HTLoadMoreUIChangeListener}
     * 和{@link HTBaseRecyclerView.HTRefreshUIChangeListener}两个接口
     * @param refreshViewHolder 自定义刷新样式对象
     */
    void setRefreshViewHolder(@NonNull HTBaseViewHolder refreshViewHolder);


    /******************以下方法和RecyclerView中的一致**************************/

    /**
     * 移除刷新控件滚动事件监听
     * @param listener 列表滚动事件监听对象
     */
    void removeOnScrollListener(HTBaseRecyclerView.OnScrollListener listener);

    /**
     * 添加刷新控件滚动事件监听
     * @param listener 列表滚动事件监听对象
     */
    void addOnScrollListener(HTBaseRecyclerView.OnScrollListener listener);

    /** 移除所有列表滚动事件的监听 */
    void clearOnScrollListeners();

    /**
     * 移除{@link RecyclerView.OnItemTouchListener},将不再拦截触摸事件<br/>
     * 详细描述{@link RecyclerView#removeOnItemTouchListener(RecyclerView.OnItemTouchListener)}
     * @param listener item触摸事件监听对象
     */
    void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener);

    /**
     * 添加{@link RecyclerView.OnItemTouchListener},监听触摸事件<br/>
     * 详细描述{@link RecyclerView#addOnItemTouchListener(RecyclerView.OnItemTouchListener)}
     * @param listener item触摸事件监听对象
     */
    void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener);

    /**
     * 获取指定view的{@link RecyclerView.ViewHolder}<br/>
     * 详细描述{@link RecyclerView#getChildViewHolder(View)}
     */
    RecyclerView.ViewHolder getChildViewHolder(View child);

    /**
     * 设置{@link RecyclerView.RecyclerListener}对象,当view被回收时回调该方法<br/>
     * 详细描述{@link RecyclerView#setRecyclerListener(RecyclerView.RecyclerListener)}
     * @param listener view回收事件监听对象
     */
    void setRecyclerListener(RecyclerView.RecyclerListener listener);

    /**
     * 获取控件的{@link RecyclerView.Adapter}<br/>
     * 详细描述{@link RecyclerView#getAdapter()}
     */
    RecyclerView.Adapter getAdapter();

    /**
     * 设置控件的Adapter数据源对象<br/>
     * 详细描述{@link RecyclerView#setAdapter(RecyclerView.Adapter)}方法
     * @param adapter 列表数据源对象
     */
    void setAdapter(RecyclerView.Adapter adapter);

    /** {@link RecyclerView#hasFixedSize()} */
    boolean hasFixedSize();

    /** {@link RecyclerView#setHasFixedSize(boolean)} */
    void setHasFixedSize(boolean hasFixedSize);

    /**
     * 移除item的装饰器<br/>
     * {@link RecyclerView#removeItemDecoration(RecyclerView.ItemDecoration)}
     * @param decor 装饰器对象
     */
    void removeItemDecoration(RecyclerView.ItemDecoration decor);

    /**
     * 添加item的装饰器到列表中
     * @param decor 装饰器对象
     */
    void addItemDecoration(RecyclerView.ItemDecoration decor);

    /**
     * 添加item的装饰器到列表中,index为负值,则添加在列表元素的下面<br/>
     * {@link RecyclerView#addItemDecoration(RecyclerView.ItemDecoration, int)}
     */
    void addItemDecoration(RecyclerView.ItemDecoration decor, int index);

    /**
     * 获取item的动画效果<br/>
     * {@link RecyclerView#getItemAnimator()}
     */
    RecyclerView.ItemAnimator getItemAnimator();

    /**
     * 设置item的动画效果<br/>
     * {@link RecyclerView#setItemAnimator(RecyclerView.ItemAnimator)}
     */
    void setItemAnimator(RecyclerView.ItemAnimator animator);

    /**
     * 获取布局管理对象<br/>
     * {@link RecyclerView#getLayoutManager()}
     */
    RecyclerView.LayoutManager getLayoutManager();

    /**
     * 设置布局管理对象<br/>
     * {@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)}
     */
    void setLayoutManager(RecyclerView.LayoutManager layoutManager);

}
