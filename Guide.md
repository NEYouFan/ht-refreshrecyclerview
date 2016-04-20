# HTRefreshRecyclerView使用文档
`HTRefreshRecyclerView` 是一种基于 [RecyclerView](http://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) 的支持刷新和加载更多功能的控件。根据[README文档](https://github.com/NEYouFan/ht-refreshrecyclerview/blob/master/README.md)，基本了解了`HTRefreshRecyclerView`的特点以及快速使用的方法。

## 基本使用
### 1.自定义刷新和加载更多视图
主要是通过继承`HTBaseViewHolder`类来完成，详情的实现示例，可以参考[demo](https://github.com/NEYouFan/ht-refreshrecyclerview/tree/master/demo)。
#### 基本参数设置
* 设置刷新视图和加载更多视图：
>重写`onInitRefreshView()`和`onInitLoadMoreView()`方法设置视图。
  
* 刷新视图和加载更多视图背景色
>使用`setRefreshViewBackgroundResId(int refreshViewBackgroundResId)`方法设置刷新视图的背景色；加载更多视图背景色方法为`setLoadMoreViewBackgroundResId(int loadMoreViewBackgroundResId)`。
    
* 手指移动距离和刷新控件移动距离比值
>使用`setPullDistanceScale(float pullDistanceScale)` 方法设置，参数不对则抛出异常。默认比例值是`1.8f`。

* 刷新控件的弹簧距离设置(最大可拉动的距离)
>刷新控件的弹簧距离通过设置与刷新控件的高度比例值进行限定，调用`setSpringDistanceScale(float springDistanceScale)` 方法设置，参数不对则抛出异常。默认比例值是`2.4f`。

* 动画时间设置
>使用`setAnimationTime(int animationTime)`设置刷新中动画执行时间，参数不对则抛出异常。默认时间是`500ms`。

#### 视图变化接口实现
实现刷新视图变化监听接口`HTRefreshUIChangeListener`和加载更多视图变化监听接口`HTLoadMoreUIChangeListener`，从而在各个界面操作视图变化。

###### HTRefreshUIChangeListener
* `onReset()`
> 控件的刷新视图重置时回调。    

* `onRefreshStart(boolean isPreStatusIdle)`
> 控件被拉动但未达到触发刷新的条件，并且手指没有移开时回调一次。由于控件的前一个状态可以是`RefreshStatus.IDLE`或者是`RefreshStatus.RELEASE_TO_REFRESH`，因此不同的状态切换可能有不同的处理方式，用`isPreStatusIdle`标示。

* `onReleaseToRefresh()`
> 控件被拉动达到刷新条件，并且手指没有移开时回调一次。手指移开则立即触发刷新。

* `onRefreshing()`  
> 控件即将处于刷新状态时回调。  

* `onRefreshComplete()`
> 控件刷新操作结束时回调。

* `onRefreshPositionChange(float scale, float moveDistance)`
>控件的刷新视图可见时(非`RefreshStatus.IDLE`和`RefreshStatus.REFRESHING`状态)回调，可以用于处理基于移动距离或者比值的视图动画操作等。`scale`的变化范围为`0~1.0f`(达到触发刷新条件以后，继续拉动，也保持`1.0f`，和`moveDistance`不同)； `moveDistance`代表刷新控件移动距离的值。

###### HTLoadMoreUIChangeListener
* `onLoadMoreStart(boolean hasMore)`
> 控件触发加载更多时回调，`hasMore`可以用于视图内容相关的逻辑处理。

* `onLoadMoreComplete(boolean hasMore)`
> 控件加载更多操作完成时回调，`hasMore`可以用于视图内容相关的逻辑处理。

### 2.刷新控件的使用
#### HTRefreshRecyclerView的定义
主要通过`XML`布局文件定义，支持`vertical_down`(默认)、`vertical_up`、`horizontal_left`、`horizontal_right`方向的刷新和加载更多。
#### HTRefreshRecyclerView的常用方法
`HTRefreshRecyclerView`是基于`RecyclerView`实现的控件，一些常用方法和`RecyclerView`保持统一（暂时只提供常用的一些方法），如`setAdapter()`和`setLayoutManager()`方法等，这里就不做详细介绍了。
##### 其他常用的自定义方法
* `setRefreshCompleted(boolean hasMore)`，设置刷新操作完成
* `startAutoRefresh()`，触发自动刷新,刷新状态的视图可见
* `startAutoLoadMore()`，触发自动加载更多,加载更多状态的视图可见
* `setLoadMoreShow(boolean loadMoreShow)`，设置没有更多数据时,加载更多视图的显示样式，默认为一直显示
* `setOnLoadMoreListener(HTLoadMoreListener loadMoreDelegate)`，设置加载更多事件监听，不设置则默认不支持加载更多功能
* `setOnRefreshListener(HTRefreshListener refreshDelegate)`，设置刷新事件监听，不设置则默认不支持刷新功能
* `setRecyclerViewDragListener(HTRecyclerViewDragListener recyclerViewDragListener)`，设置刷新方向的拖拽事件监听
* `setRefreshViewHolder(@NonNull HTBaseViewHolder refreshViewHolder)`，设置自定义的刷新样式，不设置默认使用带箭头的刷新样式
* `setRefreshViewHolderClass(@NonNull Class<? extends HTBaseViewHolder> mViewHolderClass)`，设置全局的自定义的刷新样式
* `RecyclerView getRecyclerView()`，返回控件内被包裹的`RecyclerView`对象，建议在当前的刷新控件不能满足某些功能需求的情况下使用

#### 一些功能接口定义 
##### HTRefreshListener 
`HTRefreshListener`是控件的刷新监听事件回调接口，如果用户希望控件支持刷新功能需要实现该接口。

* `onRefresh()`

    > 控件在刷新状态时回调该方法，可以在该方法内处理相关的业务逻辑。刷新完成后必须调用`setRefreshCompleted(boolean hasMore)`方法完成刷新操作。

##### HTLoadMoreListener
`HTLoadMoreListener`是控件的加载更多监听事件回调接口，如果用户希望控件支持刷新功能需要实现该接口。

* `onLoadMore()`

    > 控件在加载更多状态时回调该方法，可以在该方法内处理相关的业务逻辑。加载完成后必须调用`setRefreshCompleted(boolean hasMore)`方法完成加载操作。


##### HTRecyclerViewDragListener
`HTRecyclerViewDragListener`是控件在刷新方向上的拖拽事件监听接口，如果用户希望控件在拉动刷新或者拖拽列表滚动的时候进行相关操作，需要实现该接口。

* `onDragViewToRefresh()`    
> 在控件刷新方向上拖拽以触发刷新事件时回调。

* `onDragViewToScroll()`    
> 在控件刷新方向上拖拽使列表滚动时回调。该方法基于`RecyclerView.OnScrollListener()`事件实现，如果已经实现该接口,不用再重复添加`RecyclerView.OnScrollListener()`事件来监听列表拖拽事件。
   
   

