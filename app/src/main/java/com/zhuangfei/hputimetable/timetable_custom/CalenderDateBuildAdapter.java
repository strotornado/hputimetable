package com.zhuangfei.hputimetable.timetable_custom;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.timetable.listener.OnDateBuildAapter;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ColorUtils;

/**
 * Created by Liu ZhuangFei on 2019/2/4.
 */
public class CalenderDateBuildAdapter extends OnDateBuildAapter {
    Context context;

    public CalenderDateBuildAdapter(Context context){
        this.context=context;
    }

    /**
     * 构建月份，也就是日期栏的第一格.<br/>
     * 宽度、高度均为px
     *
     * @param mInflate
     * @param width    宽度
     * @param height   默认高度
     * @return
     */
    protected View onBuildMonthLayout(LayoutInflater mInflate, int width, int height) {
        View first = mInflate.inflate(R.layout.item_dateview_calender_first, null, false);
        //月份设置
        textViews[0] = first.findViewById(R.id.id_week_month);
        layouts[0] = null;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);

        int month = Integer.parseInt(weekDates.get(0));
        first.setLayoutParams(lp);
        textViews[0].setText(""+month);
        return first;
    }

    protected View onBuildDayLayout(LayoutInflater mInflate, int pos, int width, int height) {
        View v = mInflate.inflate(R.layout.item_dateview_calender, null, false);
        TextView dayTextView = v.findViewById(R.id.id_week_day);
        dayTextView.setText(dateArray[pos]);

        textViews[pos] = v.findViewById(R.id.id_week_date);
        layouts[pos] = v.findViewById(R.id.id_week_layout);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        layouts[pos].setLayoutParams(lp);
        textViews[pos].setText(weekDates.get(pos));
        return v;
    }

    @Override
    protected void initDateBackground() {
        for (int i = 1; i < 8; i++) {
            if (layouts[i] != null){
                textViews[i].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_dateview_normal));
                textViews[i].setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    protected void activeDateBackground(int weekDay) {
        if (textViews.length > weekDay && textViews[weekDay] != null) {
            textViews[weekDay].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_dateview_highligh));
            textViews[weekDay].setTextColor(Color.WHITE);
        }
    }

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
}
