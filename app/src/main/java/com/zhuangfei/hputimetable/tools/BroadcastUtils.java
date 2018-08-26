package com.zhuangfei.hputimetable.tools;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget;

/**
 * Created by Liu ZhuangFei on 2018/8/14.
 */

public class BroadcastUtils {

    public static void refreshAppWidget(Context context) {
        Intent intent = new Intent(ScheduleAppWidget.UPDATE_ACTION);
        intent.putExtra(ScheduleAppWidget.INT_EXTRA_START,0);
        intent.setComponent(new ComponentName(context, ScheduleAppWidget.class));
        context.sendBroadcast(intent);
    }


}
