package com.zhuangfei.hputimetable.appwidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/14.
 */

public class ScheduleService extends RemoteViewsService {
    private static final String TAG = "ScheduleService";
    private boolean isHave = true;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        Intent intent;
        Context context;

        List<Schedule> data;

        public ScheduleRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        public void onCreate() {
            data = new ArrayList<>();
        }

        @Override
        public void onDataSetChanged() {
            data.clear();
            data.addAll(findTodayData(context));
        }

        @Override
        public void onDestroy() {
            data.clear();
        }

        @Override
        public int getCount() {
            if(data==null||data.size()==0) return 1;
            return data.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            if(data!=null&&data.size()!=0){
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_item);
                if (i<0 ||i>=data.size()) return views;

                Schedule schedule = data.get(i);
                if (schedule == null) return views;

                views.setTextViewText(R.id.widget_tv_start, schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1) + "节");
                views.setTextViewText(R.id.widget_tv_name, "" + schedule.getName());
                views.setTextViewText(R.id.widget_tv_room, "" + schedule.getRoom());
                if (data.size() > 1) {
                    views.setTextViewText(R.id.widget_tv_count, (i + 1) + "/" + data.size());
                } else {
                    views.setTextViewText(R.id.widget_tv_count, "");
                }

                Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_clicklayout, fillInIntent);
                return views;
            }else{
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_empty);

                return views;
            }
        }

        public void layoutView(View v, int width, int height) {
            // validate view.width and view.height
            v.layout(0, 0, width, height);
            int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

            // validate view.measurewidth and view.measureheight
            v.measure(measuredWidth, measuredHeight);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }

        public Bitmap getViewBitmap(ViewGroup viewGroup) {
            int h = viewGroup.getHeight();
            ;
            Bitmap bitmap;

            // 创建对应大小的bitmap
            bitmap = Bitmap.createBitmap(viewGroup.getWidth(), h,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            viewGroup.draw(canvas);
            return bitmap;
        }

        @Override
        public RemoteViews getLoadingView() {
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_app_widget_empty);
//            return views;
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int i) {
            return (long) i;
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
