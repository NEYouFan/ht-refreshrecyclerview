/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview;

/**
 * 控件在刷新方向上的拖拽事件监听
 */
public interface HTRecyclerViewDragListener {

    /**
     * 拖拽列表滚动事件监听,是基于RecyclerView.OnScrollListener()事件;<br/>
     * 如果实现该接口,不用再重复添加RecyclerView.OnScrollListener()事件来监听列表拖拽事件
     */
    void onRecyclerViewScroll();

    /** 刷新视图位置准备开始变化 */
    void onRefreshViewPrepareToMove();
}
