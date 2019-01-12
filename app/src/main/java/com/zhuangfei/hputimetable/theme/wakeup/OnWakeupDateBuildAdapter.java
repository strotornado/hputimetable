package com.zhuangfei.hputimetable.theme.wakeup;

import com.zhuangfei.timetable.listener.OnDateBuildAapter;
import com.zhuangfei.timetable.model.ScheduleSupport;

/**
 * Created by Liu ZhuangFei on 2019/1/6.
 */
public class OnWakeupDateBuildAdapter extends OnDateBuildAapter {
    @Override
    public void onUpdateDate(int curWeek, int targetWeek) {
        if (textViews == null || textViews.length < 8) return;

        weekDates = ScheduleSupport.getDateStringFromWeek(curWeek, targetWeek);
        int month = Integer.parseInt(weekDates.get(0));
        textViews[0].setText(month + "\n月");
        for (int i = 1; i < 8; i++) {
            if (textViews[i] != null) {
                textViews[i].setText(weekDates.get(i));
            }
        }
    }

    public String[] getStringArray() {
        return new String[]{null, "一", "二", "三", "四", "五", "六", "日"};
    }
}
