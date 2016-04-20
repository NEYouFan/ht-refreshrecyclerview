/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 对{@link RecyclerView.ItemDecoration}进行简单封装类,用来处理加载更多view显示
 */
class HTItemDecoration extends RecyclerView.ItemDecoration {

    private RecyclerView.ItemDecoration mInnerItemDecoration;

    public HTItemDecoration(@NonNull RecyclerView.ItemDecoration itemDecoration) {
        mInnerItemDecoration = itemDecoration;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final RecyclerView.ViewHolder childViewHolder = parent.getChildViewHolder(view);
        if (childViewHolder instanceof HTWrapperAdapter.ViewHolder) {
            outRect.set(0, 0, 0, 0);//如果是加载更多的ViewHolder,不进行装饰
            return;
        }
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.getItemOffsets(outRect, view, parent, state);
        }
    }


    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
//        super.getItemOffsets(outRect, itemPosition, parent);
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.getItemOffsets(outRect, itemPosition, parent);
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDraw(c, parent, state);
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.onDraw(c, parent, state);
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
//        super.onDraw(c, parent);
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.onDraw(c, parent);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDrawOver(c, parent, state);
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.onDrawOver(c, parent, state);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent) {
//        super.onDrawOver(c, parent);
        if (mInnerItemDecoration != null) {
            mInnerItemDecoration.onDrawOver(c, parent);
        }
    }
}
