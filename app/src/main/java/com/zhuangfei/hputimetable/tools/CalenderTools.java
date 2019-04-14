package com.zhuangfei.hputimetable.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Liu ZhuangFei on 2019/4/14.
 */
public class CalenderTools {
    public static String getLastWeekSunday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat sdf=new SimpleDateFormat("d");
        calendar.add(Calendar.DATE,-7);
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        return sdf.format(calendar.getTime());
    }
}
