### v1.2.4 （2018.09.9）
* 复写RecyclerView.Adapter的onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads)方法，解决局部刷新导致加载更多的显示问题

### v1.2.3 （2017.08.02）
* 解决GridLayoutManager.SpanSizeLookup被覆盖的问题

### v1.2.1 （2017.05.26）
* 修复加载更多监听某些情况下不触发问题

### v1.2.0 （2017.04.26）
* 修复加载更多动画显示出错问题

### v1.1.1 （2017.03.21）
* 修复触发自动刷新时视图显示问题

### v1.1.0 （2017.03.13）
* 初始化刷新&加载视图一些逻辑优化调整

### v1.0.0 （2017.02.13）
* 解决HTBaseViewHolder初始化refreshView和loadMoreView为null时，引起显示错乱的问题
* 代码结构重构，更新下拉刷新度机制

### v0.1.5 （2017.01.19）
* 修复设置刷新视图背景色的问题

### v0.1.4 （2017.01.13）
* 支持刷新过程中允许列表滚动（目前仅支持刷新视图置顶）

### v0.1.3 （2016.10.11）
* 修复刷新立即结束时,刷新视图不能隐藏的问题

### v0.1.2 （2016.07.11）
* 一些小优化

### v0.1.1 （2016.06.01）
* update the version of RecyclerView to 23.2.0

### v0.1.0 （2016.04.20）
* 支持各个方向的刷新和加载更多
* 自定义刷新和加载更多的视图样式
* 支持线性布局、GridView和瀑布流
* 支持RecyclerView装饰器