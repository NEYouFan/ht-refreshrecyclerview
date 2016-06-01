/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自定义包裹的Adapter,主要用来处理加载更多视图
 */
public class HTWrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LOAD_MORE_VIEW = Integer.MIN_VALUE + 1;
    /**
     * 内部真正的Adapter
     */
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mInnerAdapter;
    /**
     * 加载更多ViewHolder生命周期监听
     */
    private LoadMoreViewHolderListener mLoadMoreViewHolderListener;
    /**
     * 自定义的加载更多view
     */
    private View loadMoreView;

    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart, itemCount);

        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart, itemCount);
            if (mLoadMoreViewHolderListener != null) {//用来处理带动画效果删除时,加载更多的显示
                mLoadMoreViewHolderListener.onItemRemoved(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            notifyItemRangeChanged(fromPosition, toPosition + itemCount);
            if (mLoadMoreViewHolderListener != null) {
                mLoadMoreViewHolderListener.onItemRemoved(fromPosition, toPosition + itemCount);
            }
        }
    };

    public HTWrapperAdapter(@NonNull RecyclerView.Adapter<RecyclerView.ViewHolder> innerAdapter) {
        this(innerAdapter, null);
    }

    public HTWrapperAdapter(@NonNull RecyclerView.Adapter<RecyclerView.ViewHolder> innerAdapter, View loadMoreView) {
        this.loadMoreView = loadMoreView;
        setInnerAdapter(innerAdapter);
    }

    public boolean isLoadMoreView(int position) {
        return position == getItemCount() - 1 && hasLoadMoreView();
    }

    public boolean hasLoadMoreView() {
        return loadMoreView != null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE_VIEW) {
            return new ViewHolder(loadMoreView);
        } else
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isLoadMoreView(position)) {
            if (mLoadMoreViewHolderListener != null) {
                mLoadMoreViewHolderListener.onBindData(holder, position);
            }
            return;
        }
        if (mInnerAdapter != null) {
            int adapterCount = mInnerAdapter.getItemCount();
            if (position < adapterCount) {
                mInnerAdapter.onBindViewHolder(holder, position);
            }
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof ViewHolder) {
            if (loadMoreView == null) return;
            //支持瀑布流布局
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
            if (mLoadMoreViewHolderListener != null) {
                mLoadMoreViewHolderListener.onViewAttachedToWindow(holder);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //对Grid布局进行支持
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return isLoadMoreView(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            if (mLoadMoreViewHolderListener != null) {
                mLoadMoreViewHolderListener.onViewDetachedFromWindow(holder);
            }
        }
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (hasLoadMoreView()) itemCount++;
        if (mInnerAdapter != null) {
            itemCount += mInnerAdapter.getItemCount();
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (mInnerAdapter != null) {
            int adapterCount = mInnerAdapter.getItemCount();
            if (position < adapterCount) {
                return mInnerAdapter.getItemViewType(position);
            }
        }
        return TYPE_LOAD_MORE_VIEW;
    }

    /**
     * 设置被包裹的Adapter（真正显示数据的Adapter）
     *
     * @param adapter 被包裹的Adapter
     */
    private void setInnerAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        if (mInnerAdapter != null) {
            notifyItemRangeRemoved(0, mInnerAdapter.getItemCount());
            mInnerAdapter.unregisterAdapterDataObserver(dataObserver);
        }
        this.mInnerAdapter = adapter;
        mInnerAdapter.registerAdapterDataObserver(dataObserver);
        notifyItemRangeInserted(0, mInnerAdapter.getItemCount());
    }

    public void setLoadMoreViewHolderListener(LoadMoreViewHolderListener initialListener) {
        mLoadMoreViewHolderListener = initialListener;
    }

    /**
     * 加载更多生命周期相关事件监听接口
     */
    public interface LoadMoreViewHolderListener {

        void onViewAttachedToWindow(RecyclerView.ViewHolder holder);

        void onBindData(RecyclerView.ViewHolder holder, int position);

        void onViewDetachedFromWindow(RecyclerView.ViewHolder holder);

        void onItemRemoved(int positionStart, int itemCount);
    }

    /**
     * 自定义的ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
