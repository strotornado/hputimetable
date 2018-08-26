package com.zhuangfei.hputimetable.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;

/**
 * Created by Liu ZhuangFei on 2018/8/14.
 */

public class ScheduleService extends RemoteViewsService {
    private static final String TAG = "ScheduleService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        Intent intent;
        Context context;

        List<Schedule> data;
        int startIndex = 0;

        public ScheduleRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        public void onCreate() {
            data = new ArrayList<>();
            data.addAll(findTodayData(context));
            startIndex = intent.getIntExtra(ScheduleAppWidget.INT_EXTRA_START, 0);
        }

        @Override
        public void onDataSetChanged() {
            data.clear();
            data.addAll(findTodayData(context));
            startIndex = ShareTools.getInt(context, ScheduleAppWidget.INT_EXTRA_START, 0);
            if (startIndex >= data.size()) startIndex = 0;
        }

        @Override
        public void onDestroy() {
            data.clear();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews itemViews = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_itemcontainer);

            if (data.size() == 0) {
                RemoteViews emptyView = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_empty);
                itemViews.addView(R.id.id_widget_item_container, emptyView);
                return itemViews;
            }

            for (int m = startIndex; m < startIndex + 2; m++) {
                addItemView(itemViews, m);
            }
            return itemViews;
        }

        public void addItemView(RemoteViews parent, int index) {
            if (index < 0 || index >= data.size()) return;
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_item);
            Schedule schedule = data.get(index);
            views.setTextViewText(R.id.id_widget_item_name, schedule.getName());
            views.setTextViewText(R.id.id_widget_item_room, schedule.getRoom());
            views.setTextViewText(R.id.id_widget_item_start, schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1));

            if (index == startIndex) {
                if (index == 0)
                    views.setImageViewResource(R.id.id_widget_item_pointer, R.drawable.ic_pointer_up_normal);
                else
                    views.setImageViewResource(R.id.id_widget_item_pointer, R.drawable.ic_pointer_up_light);

            } else {
                if (index == data.size() - 1)
                    views.setImageViewResource(R.id.id_widget_item_pointer, R.drawable.ic_pointer_down_normal);
                else
                    views.setImageViewResource(R.id.id_widget_item_pointer, R.drawable.ic_pointer_down_light);
            }

            Intent intent = new Intent();
            intent.setClass(context, ScheduleAppWidget.class);
            intent.setAction(ScheduleAppWidget.POINTER_CLICK_ACTION);

            intent.putExtra(ScheduleAppWidget.INT_EXTRA_INDEX, index);
            intent.putExtra(ScheduleAppWidget.INT_EXTRA_SIZE, data.size());
            intent.putExtra(ScheduleAppWidget.BOOLEAN_EXTRA_FIRST, index == startIndex);
            PendingIntent pi = PendingIntent.getBroadcast(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent clickPi = PendingIntent.getActivity(context, index, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.id_widget_item_pointer_layout, pi);
            views.setOnClickPendingIntent(R.id.id_widget_item_clicklayout, clickPi);
            parent.addView(R.id.id_widget_item_container, views);
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

    /**
     * 获取数据
     *
     * @return
     */
    public List<Schedule> findData(Context context) {
        if (context == null) return null;
        int id = ScheduleDao.getApplyScheduleId(this);
        List<TimetableModel> dataModels = ScheduleDao.getAllWithScheduleId(id);
        if(dataModels==null) return null;
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
        if(list==null) return new ArrayList<>();
        return list;
    }
}
