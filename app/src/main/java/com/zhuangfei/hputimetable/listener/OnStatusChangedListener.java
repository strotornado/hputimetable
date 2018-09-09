package com.zhuangfei.hputimetable.listener;

/**
 * Created by Liu ZhuangFei on 2018/9/9.
 */
public interface OnStatusChangedListener {
    void onWeekChanged(int cur);
    void onScheduleNameChanged(String scheduleName);
}
