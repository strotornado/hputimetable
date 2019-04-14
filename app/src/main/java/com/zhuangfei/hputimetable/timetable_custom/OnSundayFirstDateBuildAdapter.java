package com.zhuangfei.hputimetable.timetable_custom;

import com.zhuangfei.hputimetable.tools.CalenderTools;
import com.zhuangfei.timetable.listener.OnDateBuildAapter;
import com.zhuangfei.timetable.model.ScheduleSupport;

import java.util.Calendar;

public class OnSundayFirstDateBuildAdapter extends OnDateBuildAapter {
    @Override
    public String[] getStringArray() {
        return new String[]{null, "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    }

    @Override
    public void onUpdateDate(int curWeek, int targetWeek) {
        if (textViews == null || textViews.length < 8) return;

        weekDates = ScheduleSupport.getDateStringFromWeek(curWeek, targetWeek);
        int month = Integer.parseInt(weekDates.get(0));
        if(textViews[0]!=null){
            textViews[0].setText(month+"\n月");
        }

        Calendar calendar = Calendar.getInstance();
        int amount = targetWeek - curWeek;
        calendar.add(Calendar.WEEK_OF_YEAR, amount);

        if (textViews[1] != null) {
            textViews[1].setText(CalenderTools.getLastWeekSunday(calendar.getTime())+"日");
        }
        for (int i = 2; i < 8; i++) {
            if (textViews[i] != null) {
                textViews[i].setText(weekDates.get(i-1)+"日");
            }
        }
    }

    @Override
    protected void activeDateBackground(int weekDay) {
        if(weekDay==7) weekDay=1;
        else weekDay+=1;
        super.activeDateBackground(weekDay);
    }
}