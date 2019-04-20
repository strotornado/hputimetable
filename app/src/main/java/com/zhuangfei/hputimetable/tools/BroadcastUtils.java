package com.zhuangfei.hputimetable.tools;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget;
import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget2;
import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget3;

/**
 * Created by Liu ZhuangFei on 2018/8/14.
 */

public class BroadcastUtils {

    public static void refreshAppWidget(Context context) {
        Intent intent = new Intent(ScheduleAppWidget.UPDATE_ACTION);
        intent.putExtra(ScheduleAppWidget.INT_EXTRA_START,0);
        intent.setComponent(new ComponentName(context, ScheduleAppWidget.class));
        context.sendBroadcast(intent);

        Intent intent2 = new Intent(ScheduleAppWidget2.UPDATE_ACTION);
        intent2.setComponent(new ComponentName(context, ScheduleAppWidget2.class));
        context.sendBroadcast(intent2);

        Intent intent3 = new Intent(ScheduleAppWidget3.UPDATE_ACTION);
        intent3.setComponent(new ComponentName(context, ScheduleAppWidget3.class));
        context.sendBroadcast(intent3);
    }


}
