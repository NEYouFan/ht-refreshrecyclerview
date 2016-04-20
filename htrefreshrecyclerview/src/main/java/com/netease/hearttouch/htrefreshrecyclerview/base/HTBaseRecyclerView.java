/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.netease.hearttouch.htrefreshrecyclerview.HTLoadMoreListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRecyclerViewDragListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRefreshListener;
import com.netease.hearttouch.htrefreshrecyclerview.R;
import com.netease.hearttouch.htrefreshrecyclerview.utils.Utils;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTDefaultHorizontalRefreshViewHolder;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTDefaultVerticalRefreshViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * 刷新控件基类
 */
public abstract class HTBaseRecyclerView extends LinearLayout implements HTRefreshRecyclerViewInterface {
    private static final String TAG = HTBaseRecyclerView.class.getSimpleName();
    /** 设置全局的默认刷新加载样式 */
    private static Class<? extends HTBaseViewHolder> sViewHolderClass;
    /** 刷新监听 */
    public HTRefreshListener mRefreshDelegate;
    /** 加载更多代理监听 */
    public HTLoadMoreListener mLoadMoreDelegate;
    /** 设置刷新加载样式 */
    protected HTBaseViewHolder mHTViewHolder;
    protected RecyclerView.OnScrollListener mInnerScrollListener;
    /** 自定义的包裹的Adapter对象 */
    protected HTWrapperAdapter mHTWrapperAdapter;
    /** 真正的Adapter对象 */
    protected RecyclerView.Adapter mInnerAdapter;
    /** 是否允许没有更多数据时加载视图一直显示,默认显示 */
    protected boolean loadMoreShow = true;
    /** 包裹自定义刷新view的控件 */
    protected final LinearLayout mRefreshContainerView;
    /** 包裹自定义加载更多view的控件 */
    protected final LinearLayout mLoadMoreContainerView;
    /** 封装的RecyclerView控件 */
    protected final RecyclerView mRecyclerView;
    /** 刷新视图变化接口监听 */
    protected HTRefreshUIChangeListener mRefreshUIChangeListener;
    /** 加载视图变化接口监听 */
    protected HTLoadMoreUIChangeListener mLoadMoreUIChangeListener;
    /** 刷新控件拖拽监听 */
    private HTRecyclerViewDragListener mRecyclerViewDragListener;
    /** 加载更多状态枚举值 */
    protected int mLoadMoreStatus = LoadMoreStatus.IDLE;
    /** 刷新状态枚举值 */
    protected int mRefreshStatus = RefreshStatus.IDLE;
    /** 控件的刷新方向枚举值,默认垂直向下方向 */
    protected int mHTOrientation = Orientation.VERTICAL_DOWN;
    /** 标示加载更多的数据状态 */
    protected boolean hasMore = true;
    /** 维护自定义滚动接口列表 */
    private final ArrayList<OnScrollListener> mScrollListeners = new ArrayList<>();

    /** 刷新视图的最大最小padding值,用于显隐 */
    protected int mMinRefreshViewPadding;
    protected int mMaxRefreshViewPadding;

    protected int mInterceptTouchDownX = -1;
    protected int mInterceptTouchDownY = -1;
    protected float mRefreshDownY = -1;
    protected float mRefreshDownX = -1;
    protected int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    public HTBaseRecyclerView(Context context) {
        this(context, null, 0);
    }

    public HTBaseRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HTBaseRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        //创建界面主要控件对象
        mRecyclerView = new RecyclerView(getContext(), attrs, defStyleAttr);//根据attrs创建RecyclerView控件
        mRefreshContainerView = new LinearLayout(getContext());
        mLoadMoreContainerView = new LinearLayout(getContext());
        //一些参数初始化
        initAttrs(attrs);
        initViews();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HTRefreshRecyclerView);
        try {
            mHTOrientation = typedArray.getInteger(R.styleable.HTRefreshRecyclerView_htOrientation, 1);
        } finally {
            typedArray.recycle();
        }
    }

    private void initViews() {
        setBackgroundResource(android.R.color.transparent);//设置背景透明
        setOrientation(checkOrientationVertical() ? VERTICAL : HORIZONTAL); //设置整个界面布局的方向
        mRefreshContainerView.setGravity(Gravity.CENTER);
        mLoadMoreContainerView.setGravity(Gravity.CENTER);
        setRefreshViewLayoutParams(mRefreshContainerView);

        //设置RecyclerView的布局参数,由于是使用attrs创建RecyclerView,需要把一些参数重置
        LinearLayout.LayoutParams lp = checkOrientationVertical() ? new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0) :
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        mRecyclerView.setPadding(0, 0, 0, 0);
        lp.rightMargin = 0;
        lp.leftMargin = 0;
        lp.topMargin = 0;
        lp.bottomMargin = 0;
        lp.weight = 1;
        mRecyclerView.setLayoutParams(lp);
        mRecyclerView.setBackgroundResource(android.R.color.transparent);
        mRecyclerView.setId(View.NO_ID);//Id值不能和attrs中的重复
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);//去掉阴影

        //根据当前的方向进行布局
        removeAllViews();
        if (mHTOrientation == Orientation.VERTICAL_UP || mHTOrientation == Orientation.HORIZONTAL_LEFT) {
            addView(mRecyclerView);
            addView(mRefreshContainerView);
        } else {
            addView(mRefreshContainerView);
            addView(mRecyclerView);
        }

        //如果设置了全局的默认刷新样式,就初始化
        if (sViewHolderClass != null) {
            try {
                Constructor constructor = sViewHolderClass.getConstructor(Context.class);
                try {
                    setRefreshViewHolder((HTBaseViewHolder) constructor.newInstance(getContext()));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            HTBaseViewHolder viewHolder;
            if (checkOrientationVertical()) {
                viewHolder = new HTDefaultVerticalRefreshViewHolder(getContext());
                ((HTDefaultVerticalRefreshViewHolder) viewHolder).setDefaultRefreshViewArrow(mHTOrientation);
            } else {
                viewHolder = new HTDefaultHorizontalRefreshViewHolder(getContext());
                ((HTDefaultHorizontalRefreshViewHolder) viewHolder).setDefaultRefreshViewArrow(mHTOrientation);
            }
            setRefreshViewHolder(viewHolder);//设置默认刷新样式
        }
    }

    private void initListeners() {
        //设置RecyclerView的的触摸监听,从而屏蔽刷新时滚动导致RecyclerView的bug
        setRecyclerViewOnTouchListener();
        //设置RecyclerView的滚动监听
        setRecyclerViewOnScrollListener();
    }

    /** 设置全局的刷新样式 */
    public static void setRefreshViewHolderClass(@NonNull Class<? extends HTBaseViewHolder> mViewHolderClass) {
        HTBaseRecyclerView.sViewHolderClass = mViewHolderClass;
    }

    /** 设置刷新和加载更多的视图控件并初始化 */
    public void setRefreshViewHolder(@NonNull HTBaseViewHolder refreshViewHolder) {
        mHTViewHolder = refreshViewHolder;
        initRefreshView();
        initLoadMoreView();
    }


    private void initRefreshView() {
        if (mHTViewHolder == null) return;
        View refreshView = mHTViewHolder.getRefreshView();
        if (refreshView != null) {
            if (refreshView.getParent() != null) {
                ((ViewGroup) refreshView.getParent()).removeView(refreshView);
            }
            //获取刷新控件的尺寸
            int refreshHeaderViewSize = Utils.getItemViewSize(checkOrientationVertical() ? VERTICAL : HORIZONTAL, refreshView);
            mMinRefreshViewPadding = -refreshHeaderViewSize;
            mMaxRefreshViewPadding = (int) (refreshHeaderViewSize * mHTViewHolder.getSpringDistanceScale());
            int res = mHTViewHolder.getRefreshViewBackgroundResId();
            if (res != 0) {//默认背景透明
                mRefreshContainerView.setBackgroundResource(res);
            } else {
                mRefreshContainerView.setBackgroundResource(android.R.color.transparent);
            }
            mRefreshContainerView.removeAllViews();
            mRefreshContainerView.addView(refreshView);
            setRefreshViewLayoutParams(refreshView);
            hideRefreshView(true);
        }
        setRefreshUIChangeListener(mHTViewHolder);
    }

    private void initLoadMoreView() {
        if (mHTViewHolder == null) return;
        View loadMoreView = mHTViewHolder.getLoadMoreView();
        if (loadMoreView != null) {
            if (loadMoreView.getParent() != null) {
                ((ViewGroup) loadMoreView.getParent()).removeView(loadMoreView);
            }
            //获取加载更多控件的尺寸
            int res = mHTViewHolder.getLoadMoreViewBackgroundResId();
            if (res != 0) {//默认背景透明
                mLoadMoreContainerView.setBackgroundResource(res);
            } else {
                mLoadMoreContainerView.setBackgroundResource(android.R.color.transparent);
            }
            mLoadMoreContainerView.removeAllViews();

            mLoadMoreContainerView.addView(loadMoreView);
            setRefreshViewLayoutParams(loadMoreView);
            hideLoadMoreView(true);
        }
        setLoadMoreUIChangeListener(mHTViewHolder);
    }

    private void setRefreshViewLayoutParams(View view) {
        if (view == null) return;
        ViewGroup.LayoutParams lp = view.getLayoutParams() == null ? new ViewGroup.LayoutParams(0, 0) : view.getLayoutParams();
        lp.width = checkOrientationVertical() ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        lp.height = checkOrientationVertical() ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
        view.setLayoutParams(lp);
    }

    private boolean checkOrientationVertical() {
        return mHTOrientation == Orientation.VERTICAL_UP || mHTOrientation == Orientation.VERTICAL_DOWN;
    }


    /** 处理刷新控件状态变化 */
    protected void processRefreshStatusChanged() {
        if (mRefreshUIChangeListener == null) return;
        switch (mRefreshStatus) {
            case RefreshStatus.IDLE:
                mRefreshUIChangeListener.onReset();
                break;
          /*  case RefreshStatus.PULL_DOWN:
                mRefreshUIChangeListener.onRefreshStart();
                break;*/
            case RefreshStatus.RELEASE_TO_REFRESH:
                mRefreshUIChangeListener.onReleaseToRefresh();
                break;
            case RefreshStatus.REFRESHING:
                mRefreshUIChangeListener.onRefreshing();
                break;
            default:
                break;
        }
    }

    /** 处理刷新控件状态变化 */
    protected void processLoadMoreStatusChanged() {
        if (mLoadMoreUIChangeListener == null) return;
        switch (mLoadMoreStatus) {
            case LoadMoreStatus.IDLE:
                mLoadMoreUIChangeListener.onLoadMoreComplete(hasMore);
                break;
            case LoadMoreStatus.LOADING:
                mLoadMoreUIChangeListener.onLoadMoreStart(hasMore);
                break;
            default:
                break;
        }
    }


    /** 显隐加载更多视图 */
    public void hideLoadMoreView(boolean isHide) {
        if (mLoadMoreContainerView != null && mHTViewHolder != null) {
            int size = 0;
            if (isHide) {
                size = -getLoadMoreSize();
            }
            switch (mHTOrientation) {
                case Orientation.VERTICAL_DOWN:
                    mLoadMoreContainerView.setPadding(0, 0, 0, isHide ? size : 0);
                    break;
                case Orientation.VERTICAL_UP:
                    mLoadMoreContainerView.setPadding(0, isHide ? size : 0, 0, 0);
                    break;
                case Orientation.HORIZONTAL_LEFT:
                    mLoadMoreContainerView.setPadding(isHide ? size : 0, 0, 0, 0);
                    break;
                case Orientation.HORIZONTAL_RIGHT:
                    mLoadMoreContainerView.setPadding(0, 0, isHide ? size : 0, 0);
                    break;
            }
        }
    }

    protected int getLoadMoreSize() {
        return Utils.getItemViewSize(checkOrientationVertical() ? VERTICAL : HORIZONTAL, mHTViewHolder.getLoadMoreView());
    }

    /** 显隐刷新视图 */
    public void hideRefreshView(boolean isHide) {
        switch (mHTOrientation) {
            case Orientation.VERTICAL_DOWN:
                mRefreshContainerView.setPadding(0, isHide ? mMinRefreshViewPadding : 0, 0, 0);
                break;
            case Orientation.VERTICAL_UP:
                mRefreshContainerView.setPadding(0, 0, 0, isHide ? mMinRefreshViewPadding : 0);
                break;
            case Orientation.HORIZONTAL_LEFT:
                mRefreshContainerView.setPadding(0, 0, isHide ? mMinRefreshViewPadding : 0, 0);
                break;
            case Orientation.HORIZONTAL_RIGHT:
                mRefreshContainerView.setPadding(isHide ? mMinRefreshViewPadding : 0, 0, 0, 0);
                break;
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (shouldHandleRefresh()) {//达到刷新条件的时候,防止RecyclerView在底层禁止掉父view的拦截事件,从而导致的无法滑动
            requestDisallowInterceptTouchEvent(false);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchDownX = (int) event.getX();
                mInterceptTouchDownY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLoadMoreStatus != LoadMoreStatus.LOADING && mRefreshStatus != RefreshStatus.REFRESHING) {
                    boolean intercept;
                    int interceptTouchMoveDistanceX = (int) (event.getX() - mInterceptTouchDownX);
                    int interceptTouchMoveDistanceY = (int) (event.getY() - mInterceptTouchDownY);
                    if (mHTOrientation == Orientation.VERTICAL_UP || mHTOrientation == Orientation.VERTICAL_DOWN) {
                        if (mInterceptTouchDownY == -1) {
                            mInterceptTouchDownY = (int) event.getY();
                        }

                        boolean up = mHTOrientation == Orientation.VERTICAL_UP && interceptTouchMoveDistanceY < -mTouchSlop;
                        boolean down = mHTOrientation == Orientation.VERTICAL_DOWN && interceptTouchMoveDistanceY > mTouchSlop;
                        intercept = (up || down) && Math.abs(interceptTouchMoveDistanceX) < Math.abs(interceptTouchMoveDistanceY);
                    } else {
                        if (mInterceptTouchDownX == -1) {
                            mInterceptTouchDownX = (int) event.getX();
                        }
                        boolean left = mHTOrientation == Orientation.HORIZONTAL_LEFT && interceptTouchMoveDistanceX < -mTouchSlop;
                        boolean right = mHTOrientation == Orientation.HORIZONTAL_RIGHT && interceptTouchMoveDistanceX > mTouchSlop;
                        intercept = (left || right) && Math.abs(interceptTouchMoveDistanceX) > Math.abs(interceptTouchMoveDistanceY);
                    }
                    if (intercept && shouldHandleRefresh()) {
                        event.setAction(MotionEvent.ACTION_CANCEL);//设置ACTION_CANCEL，使子控件取消按下状态
                        super.onInterceptTouchEvent(event);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mInterceptTouchDownX = -1;
                mInterceptTouchDownY = -1;
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mHTViewHolder != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRefreshDownX = (int) event.getX();
                    mRefreshDownY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (handleMoveAction(event)) {
                        if (mRecyclerViewDragListener != null) {
                            mRecyclerViewDragListener.onDragViewToRefresh();
                        }
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (handleUpOrCancelAction(event)) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initListeners();
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter != null && !(adapter instanceof HTWrapperAdapter)) {//支持刷新更多,要求必须是HTWrapperAdapter类型
            throw new IllegalArgumentException("the type of adapter is incorrect !");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        clear();//清除内置的监听事件
        super.onDetachedFromWindow();
    }


    private void clear() {
        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.setOnTouchListener(null);
    }



    /**
     * 禁止刷新的时候滑动列表
     * 避免 RecyclerView Bug：IndexOutOfBoundsException: Inconsistency detected. Invalid item position
     */
    private void setRecyclerViewOnTouchListener() {
        mRecyclerView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mRefreshStatus == RefreshStatus.REFRESHING;
                    }
                }
        );
    }

    private void setRecyclerViewOnScrollListener() {
        mInnerScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && shouldHandleLoadMore()) {
                    startLoadMore();//停止滚动后触发加载更多
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (mRecyclerViewDragListener != null) {
                        mRecyclerViewDragListener.onDragViewToScroll();
                    }
                }
                for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                    OnScrollListener scrollListener = mScrollListeners.get(i);
                    if (scrollListener != null) {
                        scrollListener.onScrollStateChanged(recyclerView, newState);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                    OnScrollListener scrollListener = mScrollListeners.get(i);
                    if (scrollListener != null) {
                        scrollListener.onScrolled(recyclerView, dx, dy);
                    }
                }
            }
        };
        mRecyclerView.addOnScrollListener(mInnerScrollListener);
    }

    /** 处理取消或手指抬起事件 */
    protected abstract boolean handleUpOrCancelAction(MotionEvent event);

    /** 处理手指移动事件 */
    protected abstract boolean handleMoveAction(MotionEvent event);

    /** 判断是否达到刷新条件 */
    protected abstract boolean shouldHandleRefresh();

    /** 判断是否达到加载更多条件 */
    protected abstract boolean shouldHandleLoadMore();

    /** 开始刷新 */
    protected abstract void startRefresh();

    /** 开始加载更多 */
    protected abstract void startLoadMore();

    /** 结束刷新 */
    protected abstract void endRefresh();

    /** 结束加载更多 */
    protected abstract void endLoadMore();

    @Override
    public void setOnLoadMoreListener(HTLoadMoreListener loadMoreDelegate) {
        mLoadMoreDelegate = loadMoreDelegate;
    }

    @Override
    public void setOnRefreshListener(HTRefreshListener refreshDelegate) {
        mRefreshDelegate = refreshDelegate;
    }

    private void setRefreshUIChangeListener(HTRefreshUIChangeListener refreshUIChangeListener) {
        mRefreshUIChangeListener = refreshUIChangeListener;
    }

    private void setLoadMoreUIChangeListener(HTLoadMoreUIChangeListener loadMoreUIChangeListener) {
        mLoadMoreUIChangeListener = loadMoreUIChangeListener;
    }


    public void setLoadMoreViewShow(boolean loadMoreShow) {
        this.loadMoreShow = loadMoreShow;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    @Override
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        boolean reverse = mHTOrientation == Orientation.HORIZONTAL_LEFT || mHTOrientation == Orientation.VERTICAL_UP;
        int orientation = getOrientation() == VERTICAL ? OrientationHelper.VERTICAL : OrientationHelper.HORIZONTAL;
        //根据当前的刷新方向,强行设置layoutManager的属性
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager realLayoutManager = (GridLayoutManager) layoutManager;
            realLayoutManager.setReverseLayout(reverse);
            realLayoutManager.setOrientation(orientation);
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager realLayoutManager = (LinearLayoutManager) layoutManager;
            realLayoutManager.setReverseLayout(reverse);
            realLayoutManager.setOrientation(orientation);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager realLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            realLayoutManager.setReverseLayout(reverse);
            realLayoutManager.setOrientation(orientation);
        }
        mRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public RecyclerView.ItemAnimator getItemAnimator() {
        return mRecyclerView.getItemAnimator();
    }

    @Override
    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerView.setItemAnimator(animator);
    }


    @Override
    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        if (decor != null) {//用自定义的装饰器进行包裹,处理加载更多view显示
            addItemDecoration(new HTItemDecoration(decor), -1);
        }
    }


    @Override
    public void removeItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecyclerView.removeItemDecoration(decor);
    }


    @Override
    public void setHasFixedSize(boolean hasFixedSize) {
        mRecyclerView.setHasFixedSize(hasFixedSize);
    }


    @Override
    public boolean hasFixedSize() {
        return getRecyclerView().hasFixedSize();
    }


    @Override
    public RecyclerView.Adapter getAdapter() {
        return mInnerAdapter;
    }


    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter == null) return;
        //生成支持加载更多view的自定义Adapter
        mInnerAdapter = adapter;
        mHTWrapperAdapter = new HTWrapperAdapter(adapter, mLoadMoreContainerView);
        mRecyclerView.setAdapter(mHTWrapperAdapter);
    }


    @Override
    public void setRecyclerListener(RecyclerView.RecyclerListener listener) {
        mRecyclerView.setRecyclerListener(listener);
    }


    @Override
    public void clearOnScrollListeners() {
        mScrollListeners.clear();
    }


    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View child) {
        return mRecyclerView.getChildViewHolder(child);
    }


    @Override
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerView.addOnItemTouchListener(listener);
    }


    @Override
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerView.removeOnItemTouchListener(listener);
    }


    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        mScrollListeners.add(listener);
    }


    @Override
    public void removeOnScrollListener(OnScrollListener listener) {
        mScrollListeners.remove(listener);
    }

    @Override
    public void setRecyclerViewDragListener(HTRecyclerViewDragListener recyclerViewDragListener) {
        mRecyclerViewDragListener = recyclerViewDragListener;
    }

    @Override
    public void addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        if (decor != null) {
            mRecyclerView.addItemDecoration(new HTItemDecoration(decor), index);
        }
    }

    /** 刷新控件中RecyclerView的滚动事件监听接口 */
    public interface OnScrollListener {
        void onScrollStateChanged(RecyclerView recyclerView, int newState);

        void onScrolled(RecyclerView recyclerView, int dx, int dy);
    }


    /** 控件刷新方向定义 */
    public static final class Orientation {
        /** 垂直向上 */
        public static final int VERTICAL_UP = 0;
        /** 垂直向下 */
        public static final int VERTICAL_DOWN = 1;
        /** 水平向左 */
        public static final int HORIZONTAL_LEFT = 2;
        /** 水平向右 */
        public static final int HORIZONTAL_RIGHT = 3;
    }


    /** 刷新状态定义 */
    protected static class RefreshStatus {
        public static final int IDLE = 0;
        public static final int PULL_DOWN = 1;
        public static final int RELEASE_TO_REFRESH = 3;
        public static final int REFRESHING = 4;
    }

    /** 加载更多状态定义 */
    protected static class LoadMoreStatus {
        public static final int IDLE = 0;
        public static final int LOADING = 1;
    }

    /** 刷新监听接口,可以控制刷新视图在不同的阶段进行不同的操作 */
    protected interface HTRefreshUIChangeListener {
        /** 控件的刷新视图重置时回调 */
        void onReset();

        /**
         * 控件被拉动但未达到触发刷新的条件，并且手指没有移开，回调一次。由于控件的前一个状态可以是{@link RefreshStatus#IDLE}或者是
         * {@link RefreshStatus#RELEASE_TO_REFRESH},因此不同的状态切换可能有不同的处理方式
         * @param isPreStatusIdle 前一个状态是不是初始状态
         */
        void onRefreshStart(boolean isPreStatusIdle);

        /** 控件被拉动达到刷新条件，并且手指没有移开时回调一次。手指移开则立即触发刷新 */
        void onReleaseToRefresh();

        /** 控件即将处于刷新状态时回调 */
        void onRefreshing();

        /** 控件刷新操作结束时回调 */
        void onRefreshComplete();

        /**
         * 控件刷新视图可见时(非{@link RefreshStatus#IDLE}和{@link RefreshStatus#REFRESHING})回调，
         * 可以用于处理基于移动距离或者比值的视图动画操作等
         * @param scale        下拉过程0 到 1，回弹过程1 到 0
         * @param moveDistance 整个下拉刷新控件距离变化的值
         */
        void onRefreshPositionChange(float scale, float moveDistance);
    }

    /** 加载更多监听接口 */
    protected interface HTLoadMoreUIChangeListener {
        /**
         * 控件触发加载更多时回调
         * @param hasMore 可以用于视图内容相关的逻辑处理
         */
        void onLoadMoreStart(boolean hasMore);


        /**
         * 控件加载更多操作完成时回调
         * @param hasMore 可以用于视图内容相关的逻辑处理
         */
        void onLoadMoreComplete(boolean hasMore);
    }
}
