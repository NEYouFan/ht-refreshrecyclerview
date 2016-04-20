package com.netease.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.netease.demo.refreshactivity.HorizontalLeftRefreshActivity;
import com.netease.demo.refreshactivity.HorizontalRightRefreshActivity;
import com.netease.demo.refreshactivity.VerticalDownRefreshActivity;
import com.netease.demo.refreshactivity.VerticalUpRefreshActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_vertical_down_refresh).setOnClickListener(this);
        findViewById(R.id.btn_vertical_up_refresh).setOnClickListener(this);
        findViewById(R.id.btn_horizontal_right_refresh).setOnClickListener(this);
        findViewById(R.id.btn_horizontal_left_refresh).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_vertical_down_refresh: {
                VerticalDownRefreshActivity.start(this);
                break;
            }
            case R.id.btn_vertical_up_refresh: {
                VerticalUpRefreshActivity.start(this);
                break;
            }
            case R.id.btn_horizontal_right_refresh: {
                HorizontalRightRefreshActivity.start(this);
                break;
            }
            case R.id.btn_horizontal_left_refresh: {
                HorizontalLeftRefreshActivity.start(this);
                break;
            }

        }
    }


}
