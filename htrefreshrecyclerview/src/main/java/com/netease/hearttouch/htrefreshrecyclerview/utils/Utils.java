/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 共用的一些工具方法辅助类
 */
public class Utils {

    /**
     * 获取第一个可见/完全可见item的位置索引
     * @param layoutManager       RecyclerView.LayoutManager对象
     * @param isCompletelyVisible 是否完全可见
     * @return item的索引
     */
    public static int getFirstItemPosition(@NonNull RecyclerView.LayoutManager layoutManager, boolean isCompletelyVisible) {
        int firstVisibleItemPosition = -1;
        if (layoutManager instanceof LinearLayoutManager) { //GridLayoutManager继承LinearLayoutManager
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            firstVisibleItemPosition = isCompletelyVisible ? linearLayoutManager.findFirstCompletelyVisibleItemPosition() : linearLayoutManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] firstVisibleItemPositions = isCompletelyVisible ? staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null) : staggeredGridLayoutManager.findFirstVisibleItemPositions(null);
            if (firstVisibleItemPositions != null && firstVisibleItemPositions.length > 0)
                firstVisibleItemPosition = firstVisibleItemPositions[0];
        }
        return firstVisibleItemPosition;
    }

    /**
     * 获取最后一个可见/完全可见item的位置索引
     * @param layoutManager       RecyclerView.LayoutManager对象
     * @param isCompletelyVisible 是否完全可见
     * @return item的索引
     */
    public static int getLastVisibleItemPosition(@NonNull RecyclerView.LayoutManager layoutManager, boolean isCompletelyVisible) {
        int lastVisibleItemPosition = -1;
        if (layoutManager instanceof LinearLayoutManager) {//GridLayoutManager继承LinearLayoutManager
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            lastVisibleItemPosition = isCompletelyVisible ? linearLayoutManager.findLastCompletelyVisibleItemPosition() : linearLayoutManager.findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastVisibleItemPositions = isCompletelyVisible ? staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null) : staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
            if (lastVisibleItemPositions != null && lastVisibleItemPositions.length > 0)
                lastVisibleItemPosition = findMax(lastVisibleItemPositions);
        }
        return lastVisibleItemPosition;
    }


    /**
     * 取数组中最大值
     * @param array 数组对象
     * @return 数组的最大值
     */
    private static int findMax(int[] array) {
        int max = array[0];
        for (int value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * 获新控件的高度或者宽度
     * @param orientation 1:高度 0:宽度
     * @param view        被测量的控件对象
     * @return view的测量尺寸值
     */
    public static int getItemViewSize(int orientation, View view) {
        if (view != null) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(View.MEASURED_SIZE_MASK, View.MeasureSpec.AT_MOST);
            view.measure(measureSpec, measureSpec);
            return orientation == LinearLayout.VERTICAL ? view.getMeasuredHeight() : view.getMeasuredWidth();
        }
        return 0;
    }
}
