package com.zhuangfei.hputimetable.listener;

import com.zhuangfei.hputimetable.api.model.ScheduleName;

/**
 * Created by Liu ZhuangFei on 2018/9/9.
 */
public interface OnSwitchTableListener {
    void onSwitchTable(ScheduleName scheduleName);
}
