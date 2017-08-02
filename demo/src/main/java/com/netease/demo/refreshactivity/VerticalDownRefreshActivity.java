package com.netease.demo.refreshactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.netease.demo.DividerItemDecoration;
import com.netease.demo.MyAdapter;
import com.netease.demo.R;
import com.netease.demo.refreshstyle.DotStyleVerticalDownRefreshViewHolder;
import com.netease.hearttouch.htrefreshrecyclerview.HTLoadMoreListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRefreshListener;
import com.netease.hearttouch.htrefreshrecyclerview.HTRefreshRecyclerView;
import com.netease.hearttouch.htrefreshrecyclerview.base.HTBaseViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

public class VerticalDownRefreshActivity extends AppCompatActivity implements HTLoadMoreListener, HTRefreshListener, View.OnClickListener {
    private final ArrayList<String> mData = new ArrayList<>();
    private HTRefreshRecyclerView mRefreshLayout;

    public static void start(Context context) {
//        HTRefreshRecyclerView.setRefreshViewHolderClass(DotStyleVerticalDownRefreshViewHolder.class);
        Intent intent = new Intent(context, VerticalDownRefreshActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertaical_refresh);
        initView();
    }

    public void initView() {
        String[] data = getResources().getStringArray(R.array.cat_names);
        mData.addAll(Arrays.asList(data).subList(0, 8));
        MyAdapter myAdapter = new MyAdapter(mData);
        mRefreshLayout = (HTRefreshRecyclerView) findViewById(R.id.vertical_refresh);
        //设置刷新样式的视图,HTBaseViewHolder,具体可查看demo中DotStyleRefreshViewHolder实现
        HTBaseViewHolder viewHolder = new DotStyleVerticalDownRefreshViewHolder(this);
//        viewHolder.setLoadMoreViewBackgroundResId(android.R.color.holo_red_light);
//        viewHolder.setRefreshViewBackgroundResId(android.R.color.holo_red_light);
        mRefreshLayout.setRefreshViewHolder(viewHolder);//不设置样式,则使用默认箭头样式
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        // layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);//设置列表布局方式
//        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);//设置瀑布流布局方式
         layoutManager = new GridLayoutManager(this, 2);//设置列表布局方式
        mRefreshLayout.setLayoutManager(layoutManager);//设置列表布局方式
        mRefreshLayout.setEnableScrollOnRefresh(true);
        mRefreshLayout.addItemDecoration(new DividerItemDecoration(DividerItemDecoration.VERTICAL));
        mRefreshLayout.setAdapter(myAdapter);//设置数据源
        mRefreshLayout.setOnLoadMoreListener(this);//实现OnLoadMoreListener接口
        mRefreshLayout.setOnRefreshListener(this);//实现OnRefreshListener接口
//        mRefreshLayout.setLoadMoreViewShow(false);
        findViewById(R.id.button_refresh).setOnClickListener(this);
        findViewById(R.id.button_load_more).setOnClickListener(this);
    }

    @Override
    public void onLoadMore() {
        if (mData.size() > 20) {
            mRefreshLayout.setRefreshCompleted(false);//设置为false表示加载完毕
        } else {
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < Math.min(mData.size(), 3); i++) {
                        mData.add(mData.get(i)+""+i);
                    }
                    mRefreshLayout.getAdapter().notifyDataSetChanged();
                    mRefreshLayout.setRefreshCompleted(true);
                    Toast.makeText(VerticalDownRefreshActivity.this, "loadMore Completed", Toast.LENGTH_SHORT).show();

                }
            }, 2000);
        }
    }


    protected void onResume() {
        super.onResume();
//       getWindow().getDecorView().post(new Runnable() {
//           public void run() {
//               mRefreshLayout.startAutoRefresh();
//           }
//       }) ;
    }

    @Override
    public void onRefresh() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                for (int i = 0; i < Math.min(mData.size(), 3); i++) {
                    mData.add(0, mData.get(i));
                }
                mRefreshLayout.getAdapter().notifyDataSetChanged();
                mRefreshLayout.setRefreshCompleted(true);
                Toast.makeText(VerticalDownRefreshActivity.this, "refresh Completed", Toast.LENGTH_SHORT).show();
            }

        }, 2000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_refresh: {
                mRefreshLayout.startAutoRefresh();
//                mRefreshLayout.getAdapter().notifyItemRemoved(mRefreshLayout.getAdapter().getItemCount() - 1);
//                mData.remove(mRefreshLayout.getAdapter().getItemCount() - 1);
                break;
            }
            case R.id.button_load_more: {
                mRefreshLayout.startAutoLoadMore();
                break;
            }
        }
    }
}
