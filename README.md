## HTRefreshRecyclerView

`HTRefreshRecyclerView` 是一种基于 [RecyclerView](http://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) 的支持刷新和加载更多功能的控件。

|默认样式 |[demo](https://github.com/NEYouFan/ht-refreshrecyclerview/tree/master/demo)自定义样式|
|---|---|
| ![image](https://github.com/NEYouFan/ht-refreshrecyclerview/raw/master/gif/Untitled1.gif?raw=true)|![image](https://github.com/NEYouFan/ht-refreshrecyclerview/raw/master/gif/Untitled2.gif?raw=true)|


## 特性
* 支持各个方向的刷新和加载更多
* 自定义刷新和加载更多的视图样式
* 支持线性布局、`GridView`和瀑布流

## 用法
### 在XML布局文件中使用控件
```
<com.netease.hearttouch.htrefreshrecyclerview.HTRefreshRecyclerView
    android:id="@+id/vertical_refresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:htOrientation="vertical_down"/>
```

支持设置方向`vertical_down`(默认)、`vertical_up`、`horizontal_left`、`horizontal_right`。

### 自定义刷新和加载更多视图样式
若用户不设置刷新样式，会自动使用默认的刷新样式（示例图-默认样式）；

若用户需要自定义刷新样式，则需要继承`HTBaseViewHolder`类实现相应的接口，并通过`setRefreshViewHolder()`方法设置局部样式或者`setRefreshViewHolderClass()`方法设置全局样式。详细使用说明可参考[demo](https://github.com/NEYouFan/ht-refreshrecyclerview/tree/master/demo)或者[使用文档](https://github.com/NEYouFan/ht-refreshrecyclerview/blob/master/Guide.md)。

### 设置数据和布局方式
 数据源和布局的设置和 `RecyclerView` 控件一致，例如
 
```
 mRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));//设置列表布局方式
 mRefreshRecyclerView.setAdapter(myAdapter);//设置数据源
```
### 实现刷新和加载更多事件监听
```
 //若实现HTRefreshListener接口，支持刷新，不设置则默认不支持刷新
 mRefreshRecyclerView.setOnRefreshListener(new HTRefreshListener(){...});
 //若实现HTLoadMoreListener接口，支持加载更多，不设置则默认不支持加载更多
 mRefreshRecyclerView.setOnLoadMoreListener(new HTLoadMoreListener(){...});
```
   
## 集成

### Gradle

```
compile 'com.netease.hearttouch:ht-refreshrecyclerview:1.2.3'
```

### Maven

```
<dependency>
  <groupId>com.netease.hearttouch</groupId>
  <artifactId>ht-refreshrecyclerview</artifactId>
  <version>1.2.3</version>
</dependency>
```

## 混淆
如果要使用混淆，在引用工程的 `proguard` 文件中，添加如下代码：

```
-keep class com.netease.hearttouch.htrefreshrecyclerview.** { *; }
-dontwarn com.netease.hearttouch.htrefreshrecyclerview.**
```

## 版本更新
[更新记录](https://github.com/NEYouFan/ht-refreshrecyclerview/blob/master/CHANGELOG.md)

## 许可证
`HTRefreshRecyclerView` 使用 `MIT` 许可证，详情见 [LICENSE](https://github.com/NEYouFan/ht-refreshrecyclerview/blob/master/LICENSE.txt) 文件。