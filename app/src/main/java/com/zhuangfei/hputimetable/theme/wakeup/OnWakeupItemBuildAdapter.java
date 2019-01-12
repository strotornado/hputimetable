package com.zhuangfei.hputimetable.theme.wakeup;

import android.graphics.drawable.GradientDrawable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zhuangfei.timetable.listener.OnItemBuildAdapter;
import com.zhuangfei.timetable.model.Schedule;

/**
 * Created by Liu ZhuangFei on 2019/1/6.
 */
public class OnWakeupItemBuildAdapter extends OnItemBuildAdapter {
    @Override
    public void onItemUpdate(FrameLayout layout, TextView textView, TextView countTextView, Schedule schedule, GradientDrawable gd) {
        super.onItemUpdate(layout, textView, countTextView, schedule, gd);

    }
}
