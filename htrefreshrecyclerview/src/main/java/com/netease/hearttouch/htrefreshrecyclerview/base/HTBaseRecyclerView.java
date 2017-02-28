/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.netease.hearttouch.htrefreshrecyclerview.HTLoadMoreListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRecyclerViewDragListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRefreshListener;
import com.netease.hearttouch.htrefreshrecyclerview.R;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTDefaultHorizontalRefreshViewHolder;
import com.netease.hearttouch.htrefreshrecyclerview.viewimpl.HTDefaultVerticalRefreshViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import static com.netease.hearttouch.htrefreshrecyclerview.base.HTOrientation.VERTICAL_DOWN;
import static com.netease.hearttouch.htrefreshrecyclerview.base.HTOrientation.VERTICAL_UP;

/**
 * 刷新控件基类
 */
public abstract class HTBaseRecyclerView extends ViewGroup implements HTRefreshRecyclerViewInterface {
    private static final String TAG = HTBaseRecyclerView.class.getSimpleName();
    /**
     * 设置全局的默认刷新加载样式
     */
    private static Class<? extends HTBaseViewHolder> sViewHolderClass;
    /**
     * 刷新监听
     */
    public HTRefreshListener mRefreshDelegate;
    /**
     * 加载更多代理监听
     */
    public HTLoadMoreListener mLoadMoreDelegate;
    /**
     * 设置刷新加载样式
     */
    protected HTBaseViewHolder mHTViewHolder;

    protected HTViewHolderTracker mHTViewHolderTracker;

    protected RecyclerView.OnScrollListener mInnerScrollListener;
    /**
     * 自定义的包裹的Adapter对象
     */
    protected HTWrapperAdapter mHTWrapperAdapter;
    /**
     * 真正的Adapter对象
     */
    protected RecyclerView.Adapter mInnerAdapter;
    /**
     * 是否允许没有更多数据时加载视图一直显示(不满一屏幕一直隐藏),默认显示
     */
    protected boolean mLoadMoreViewDisplay = true;
    /**
     * 包裹自定义刷新view的控件
     */
    protected final ViewGroup mRefreshContainerView;
    /**
     * 包裹自定义加载更多view的控件
     */
    protected final ViewGroup mLoadMoreContainerView;
    /**
     * 封装的RecyclerView控件
     */
    protected final RecyclerView mRecyclerView;
    /**
     * 刷新视图变化接口监听
     */
    protected HTRefreshUIChangeListener mRefreshUIChangeListener;
    /**
     * 加载视图变化接口监听
     */
    protected HTLoadMoreUIChangeListener mLoadMoreUIChangeListener;
    /**
     * 刷新控件拖拽监听
     */
    protected HTRecyclerViewDragListener mRecyclerViewDragListener;
    /**
     * 加载更多状态枚举值
     */
    protected int mLoadMoreStatus = LoadMoreStatus.IDLE;
    /**
     * 刷新状态枚举值
     */
    protected int mRefreshStatus = RefreshStatus.IDLE;
    /**
     * 控件的刷新方向枚举值,默认垂直向下方向
     */
    protected int mHTOrientation = VERTICAL_DOWN;
    /**
     * 标示加载更多的数据状态
     */
    protected boolean mHasMore = true;
    /**
     * 维护自定义滚动接口列表
     */
    private final ArrayList<OnScrollListener> mScrollListeners = new ArrayList<>();

    /**
     * 是否允许刷新的时候，界面滚动
     */
    protected boolean mEnableScrollOnReFresh = false;

    protected final int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private Paint mRefreshBgPaint;


    public HTBaseRecyclerView(Context context) {
        this(context, null, 0);
    }

    public HTBaseRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HTBaseRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        setWillNotDraw(false);
        //创建界面主要控件对象
        mRecyclerView = new RecyclerView(getContext(), attrs, defStyleAttr);//根据attrs创建RecyclerView控件
        mRefreshContainerView = new FrameLayout(getContext());
        mLoadMoreContainerView = new LinearLayout(getContext());
        //一些参数初始化
        initRefreshBgPaint(context);
        initAttrs(attrs);
        initViews();
    }

    private void initRefreshBgPaint(Context context) {
        mRefreshBgPaint = new Paint();
        mRefreshBgPaint.setColor(ContextCompat.getColor(context, android.R.color.transparent));
        mRefreshBgPaint.setStyle(Paint.Style.FILL);
        mRefreshBgPaint.setAntiAlias(true);
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
        //设置RecyclerView的布局参数,由于是使用attrs创建RecyclerView,需要把一些参数重置
        mRecyclerView.setId(View.NO_ID);//Id值不能和attrs中的重复
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);//去掉阴影

        //根据当前的方向进行布局
        removeAllViews();
        mRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setViewLayoutParams(mRefreshContainerView);
        setViewLayoutParams(mLoadMoreContainerView);
        addView(mRecyclerView);
        addView(mRefreshContainerView);

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
//        setRecyclerViewOnTouchListener();
        //设置RecyclerView的滚动监听
        setRecyclerViewOnScrollListener();
    }

    /**
     * 设置全局的刷新样式
     */
    public static void setRefreshViewHolderClass(@NonNull Class<? extends HTBaseViewHolder> mViewHolderClass) {
        HTBaseRecyclerView.sViewHolderClass = mViewHolderClass;
    }

    /**
     * 设置刷新和加载更多的视图控件并初始化
     */
    public void setRefreshViewHolder(@NonNull HTBaseViewHolder refreshViewHolder) {
        mHTViewHolder = refreshViewHolder;
        mHTViewHolderTracker = mHTViewHolder.getViewHolderTracker();
        mHTViewHolderTracker.setOrientation(mHTOrientation);
        mHTViewHolder.computeViewSize(checkOrientationVertical() ? VERTICAL : HORIZONTAL);

        resetRefreshViewHolderView();
        initRefreshView();
        initLoadMoreView();
    }


    private void resetRefreshViewHolderView() {
        mRefreshContainerView.removeAllViews();
        mLoadMoreContainerView.removeAllViews();
    }


    private void initRefreshView() {
        if (mHTViewHolder == null) return;
        View refreshView = mHTViewHolder.getRefreshView();
        if (refreshView != null) {
            if (refreshView.getParent() != null) {
                ((ViewGroup) refreshView.getParent()).removeView(refreshView);
            }
            int res = mHTViewHolder.getRefreshViewBackgroundResId();
            if (res != 0) {//默认背景透明
                mRefreshContainerView.setBackgroundResource(res);
            } else {
                mRefreshContainerView.setBackgroundResource(android.R.color.transparent);
            }
            mRefreshContainerView.removeAllViews();
            setViewLayoutParams(refreshView);
            mRefreshContainerView.addView(refreshView);
//            hideRefreshView(true);
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
            int res = mHTViewHolder.getLoadMoreViewBackgroundResId();
            if (res != 0) {//默认背景透明
                mLoadMoreContainerView.setBackgroundResource(res);
            } else {
                mLoadMoreContainerView.setBackgroundResource(android.R.color.transparent);
            }
            mLoadMoreContainerView.removeAllViews();
            setViewLayoutParams(loadMoreView);
            mLoadMoreContainerView.addView(loadMoreView);
            hideLoadMoreView(true);
        }
        setLoadMoreUIChangeListener(mHTViewHolder);
    }

    private void setViewLayoutParams(View view) {
        if (view == null) return;
        ViewGroup.LayoutParams lp = view.getLayoutParams() == null ? new ViewGroup.LayoutParams(0, 0) : view.getLayoutParams();
        lp.width = checkOrientationVertical() ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        lp.height = checkOrientationVertical() ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
        view.setLayoutParams(lp);
    }

    protected boolean checkOrientationVertical() {
        return mHTOrientation == VERTICAL_UP || mHTOrientation == VERTICAL_DOWN;
    }

    protected boolean checkOrientationReverse() {
        return mHTOrientation == HTOrientation.HORIZONTAL_LEFT || mHTOrientation == VERTICAL_UP;
    }


    private void checkChildren() {
        final int childCount = getChildCount();
        if (childCount > 2) {
            throw new IllegalStateException("HTRefreshRecyclerView can only contains 2 children");
        }
    }

    public void addView(View child) {
        checkChildren();
        super.addView(child);
    }

    public void addView(View child, int index) {
        checkChildren();
        super.addView(child, index);
    }

    public void addView(View child, int index, LayoutParams params) {
        checkChildren();
        super.addView(child, index, params);
    }

    public void addView(View child, LayoutParams params) {
        checkChildren();
        super.addView(child, params);
    }

    public void addView(View child, int width, int height) {
        checkChildren();
        super.addView(child, width, height);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mRefreshContainerView != null) {
            measureChild(mRefreshContainerView, widthMeasureSpec, heightMeasureSpec);
        }
        if (mRecyclerView != null) {
            measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
        if (checkOrientationVertical()) {
            layoutVertical(mHTOrientation == VERTICAL_DOWN);
        } else {
            layoutHorizontal(mHTOrientation == HTOrientation.HORIZONTAL_RIGHT);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mHTViewHolderTracker.isOverRefreshViewSize() && mHTViewHolder.getRefreshViewBackgroundResId() > 0) {
            int drawOffset = Math.abs(mHTViewHolderTracker.getCurrentPos() - mHTViewHolderTracker.getRefreshViewSize());
            mRefreshBgPaint.setColor(ContextCompat.getColor(getContext(), mHTViewHolder.getRefreshViewBackgroundResId()));
            switch (mHTOrientation) {
                case VERTICAL_DOWN:
                    canvas.drawRect(0, 0, getWidth(), drawOffset, mRefreshBgPaint);
                    break;
                case HTOrientation.VERTICAL_UP:
                    canvas.drawRect(0, getHeight() - drawOffset, getWidth(), getHeight(), mRefreshBgPaint);
                    break;
                case HTOrientation.HORIZONTAL_RIGHT:
                    canvas.drawRect(0, 0, drawOffset, getHeight(), mRefreshBgPaint);
                    break;
                case HTOrientation.HORIZONTAL_LEFT:
                    canvas.drawRect(getWidth() - drawOffset, 0, getWidth(), getHeight(), mRefreshBgPaint);
                    break;
            }

        }
    }

    void layoutVertical(boolean isTop) {
        int offset = mHTViewHolderTracker.getCurrentPos();
        int size = mHTViewHolderTracker.getRefreshViewSize();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        int left, top, right, bottom;

        if (mRefreshContainerView != null) {
            left = paddingLeft;
            if (isTop) {
                top = -(size - paddingTop - offset);
            } else {
                top = mRecyclerView.getMeasuredHeight() - offset;
            }
            right = left + mRefreshContainerView.getMeasuredWidth();
            bottom = top + mRefreshContainerView.getMeasuredHeight();
            mRefreshContainerView.layout(left, top, right, bottom);
        }
        if (mRecyclerView != null) {
            left = paddingLeft;
            if (isTop) {
                top = paddingTop + offset;
            } else {
                top = paddingTop - offset;
            }
            right = left + mRecyclerView.getMeasuredWidth();
            bottom = top + mRecyclerView.getMeasuredHeight();
            mRecyclerView.layout(left, top, right, bottom);
        }


    }

    void layoutHorizontal(boolean isLeft) {
        int offset = mHTViewHolderTracker.getCurrentPos();
        int size = mHTViewHolderTracker.getRefreshViewSize();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        int left, top, right, bottom;
        if (mRefreshContainerView != null) {
            if (isLeft) {
                left = -(size - paddingLeft - offset);
            } else {
                left = mRecyclerView.getMeasuredWidth() - offset;
            }
            top = paddingTop;
            right = left + mRefreshContainerView.getMeasuredWidth();
            bottom = top + mRefreshContainerView.getMeasuredHeight();
            mRefreshContainerView.layout(left, top, right, bottom);
        }
        if (mRecyclerView != null) {
            if (isLeft) {
                left = paddingLeft + offset;
            } else {
                left = paddingLeft - offset;
            }
            top = paddingTop;
            right = left + mRecyclerView.getMeasuredWidth();
            bottom = top + mRecyclerView.getMeasuredHeight();
            mRecyclerView.layout(left, top, right, bottom);
        }
    }


    /**
     * 触发刷新的条件判断,是否可以在刷新方向上继续滚动
     */
    public boolean checkChildScroll() {
        switch (mHTOrientation) {
            case VERTICAL_UP:
                return ViewCompat.canScrollVertically(mRecyclerView, 1);
            case VERTICAL_DOWN:
                return ViewCompat.canScrollVertically(mRecyclerView, -1);
            case HTOrientation.HORIZONTAL_LEFT:
                return ViewCompat.canScrollHorizontally(mRecyclerView, 1);
            case HTOrientation.HORIZONTAL_RIGHT:
                return ViewCompat.canScrollHorizontally(mRecyclerView, -1);
            default:
                return false;
        }

    }


    /**
     * 处理刷新控件状态变化
     */
    protected void processLoadMoreStatusChanged() {
        if (mLoadMoreUIChangeListener == null) return;
        switch (mLoadMoreStatus) {
            case LoadMoreStatus.IDLE:
                mLoadMoreUIChangeListener.onLoadMoreComplete(mHasMore);
                break;
            case LoadMoreStatus.LOADING:
                mLoadMoreUIChangeListener.onLoadMoreStart(mHasMore);
                break;
            default:
                break;
        }
    }


    /**
     * 显隐加载更多视图
     */
    public void hideLoadMoreView(boolean isHide) {
        if (mLoadMoreContainerView != null && mHTViewHolder != null) {
            int size = 0;
            if (isHide) {
                size = -mHTViewHolderTracker.getLoadMoreSize();
            }
            switch (mHTOrientation) {
                case VERTICAL_DOWN:
                    mLoadMoreContainerView.setPadding(0, 0, 0, isHide ? size : 0);
                    break;
                case VERTICAL_UP:
                    mLoadMoreContainerView.setPadding(0, isHide ? size : 0, 0, 0);
                    break;
                case HTOrientation.HORIZONTAL_LEFT:
                    mLoadMoreContainerView.setPadding(isHide ? size : 0, 0, 0, 0);
                    break;
                case HTOrientation.HORIZONTAL_RIGHT:
                    mLoadMoreContainerView.setPadding(0, 0, isHide ? size : 0, 0);
                    break;
            }
        }
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

    public void setEnableScrollOnRefresh(boolean enableScrollOnReFresh) {
        mEnableScrollOnReFresh = enableScrollOnReFresh;
    }

    /**
     * 禁止刷新的时候滑动列表
     * 避免 RecyclerView Bug：IndexOutOfBoundsException: Inconsistency detected. Invalid item position
     */

 /*   private void setRecyclerViewOnTouchListener() {
        mRecyclerView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return !mEnableScrollOnReFresh && mRefreshStatus == RefreshStatus.REFRESHING;
                    }
                }
        );
    }*/
    private void setRecyclerViewOnScrollListener() {
        mInnerScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && shouldHandleLoadMore()) {
                    performLoadMore();//停止滚动后触发加载更多
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (mRecyclerViewDragListener != null) {
                        mRecyclerViewDragListener.onRecyclerViewScroll();
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

    /**
     * 处理手指移动事件
     */
    protected abstract Boolean handleMoveAction(MotionEvent event);

    /**
     * 判断是否达到刷新条件
     */
    protected abstract boolean shouldHandleRefresh();

    /**
     * 判断是否达到加载更多条件
     */
    protected abstract boolean shouldHandleLoadMore();

    /**
     * 开始刷新
     */
    protected abstract void performRefresh();

    /**
     * 开始加载更多
     */
    protected abstract void performLoadMore();

    /**
     * 结束刷新
     */
    protected abstract void endRefresh();

    /**
     * 结束加载更多
     */
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
        this.mLoadMoreViewDisplay = loadMoreShow;
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
        boolean reverse = checkOrientationReverse();
        int orientation = checkOrientationVertical() ? OrientationHelper.VERTICAL : OrientationHelper.HORIZONTAL;
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

    /**
     * 刷新状态定义
     */
    public static class RefreshStatus {
        /**
         * 初始状态
         */
        public static final int IDLE = 0;
        /**
         * 刷新准备状态
         */
        public static final int REFRESH_PREPARE = 1;
        /**
         * 刷新状态
         */
        public static final int REFRESHING = 2;
        /**
         * 刷新完成状态
         */
        public static final int COMPLETE = 3;
    }

    /**
     * 加载更多状态定义
     */
    public static class LoadMoreStatus {
        public static final int IDLE = 0;
        public static final int LOADING = 1;
    }

    /**
     * 刷新控件中RecyclerView的滚动事件监听接口
     */
    public interface OnScrollListener {
        void onScrollStateChanged(RecyclerView recyclerView, int newState);

        void onScrolled(RecyclerView recyclerView, int dx, int dy);
    }

    /**
     * 刷新监听接口,可以控制刷新视图在不同的阶段进行不同的操作
     */
    protected interface HTRefreshUIChangeListener {
        /**
         * 控件的刷新视图重置时回调
         */
        void onReset();

        /**
         * 控件被滑动情况下回调
         */
        void onRefreshPrepare();

        /**
         * 控件处于刷新状态时回调
         */
        void onRefreshing();

        /**
         * 控件刷新操作完成时回调
         */
        void onRefreshComplete();

        /**
         * 控件刷新视图可见时(非{@link RefreshStatus#IDLE}和{@link RefreshStatus#REFRESHING})回调，
         * 可以用于处理基于移动距离或者比值的视图动画操作等
         *
         * @param scale             下拉过程0 到 1，回弹过程1 到 0
         * @param moveDistance      整个下拉刷新控件距离变化的值
         * @param refreshStatus     下拉刷新状态，包含{@link RefreshStatus#IDLE}，{@link RefreshStatus#REFRESH_PREPARE}，{@link RefreshStatus#REFRESHING}，{@link RefreshStatus#COMPLETE}
         * @param viewHolderTracker 刷新视图滑动数据跟踪，参见{@link HTViewHolderTracker}
         */
        void onRefreshPositionChange(float scale, float moveDistance, int refreshStatus, HTViewHolderTracker viewHolderTracker);
    }

    /**
     * 加载更多监听接口
     */
    protected interface HTLoadMoreUIChangeListener {
        /**
         * 控件触发加载更多时回调
         *
         * @param hasMore 可以用于视图内容相关的逻辑处理
         */
        void onLoadMoreStart(boolean hasMore);


        /**
         * 控件加载更多操作完成时回调
         *
         * @param hasMore 可以用于视图内容相关的逻辑处理
         */
        void onLoadMoreComplete(boolean hasMore);
    }
}
