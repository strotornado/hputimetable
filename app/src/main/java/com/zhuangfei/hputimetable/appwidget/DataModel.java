package com.zhuangfei.hputimetable.appwidget;

import android.content.Context;

import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/11/14.
 */
public class DataModel {
    /**
     * 获取数据
     *
     * @return
     */
    public List<Schedule> findData(Context context) {
        if (context == null) return null;
        int id = ScheduleDao.getApplyScheduleId(context);
        List<TimetableModel> dataModels = ScheduleDao.getAllWithScheduleId(id);
        if (dataModels == null) return null;
        return ScheduleSupport.transform(dataModels);
    }

    public List<Schedule> findTodayData(Context context) {
        List<Schedule> allModels = findData(context);
        if (allModels == null) return new ArrayList<>();
        int curWeek = TimetableTools.getCurWeek(context);
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek - 2;
        if (dayOfWeek == -1) dayOfWeek = 6;
        List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
        if (list == null) return new ArrayList<>();
        return list;
    }
}
