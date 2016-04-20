/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview;

/**
 * 控件的刷新监听事件回调接口，如果用户希望控件支持刷新功能需要实现该接口
 */
public interface HTRefreshListener {

    void onRefresh();
}
