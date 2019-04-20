package com.zhuangfei.hputimetable.appwidget;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.timetable.listener.OnDateBuildAapter;

/**
 * Created by Liu ZhuangFei on 2018/10/27.
 */
public class CustomDateBuildAdapter extends OnDateBuildAapter{
    public int color= Color.BLACK;
    public float textSize=15;
    public void setColor(int color) {
        this.color = color;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int perDayWidth=0;

    public int perMonthWidth=0;

    public void setPerMonthWidth(int perMonthWidth) {
        this.perMonthWidth = perMonthWidth;
    }

    public void setPerDayWidth(int perDayWidth) {
        this.perDayWidth = perDayWidth;
    }

    public float getPerDayWidth() {
        return perDayWidth;
    }

    public float getPerMonthWidth() {
        return perMonthWidth;
    }

    @Override
    public void onHighLight() {
        super.onHighLight();
        if(textViews==null) return;
        for(int i=0;i<textViews.length;i++){
            TextView v=textViews[i];
            if(v==null) continue;
            v.setTextColor(color);
            v.setTextSize(textSize);
        }
    }

    @Override
    public View[] getDateViews(LayoutInflater mInflate, float monthWidth, float perWidth, int height) {
        View[] views = new View[8];
        views[0] = onBuildMonthLayout(mInflate, (int) monthWidth, height);
        for (int i = 1; i < 8; i++) {
            views[i] = onBuildDayLayout(mInflate, i, perMonthWidth, height);
        }
        return views;
    }

    protected View onBuildDayLayout(LayoutInflater mInflate, int pos, int width, int height) {
        View v = mInflate.inflate(com.zhuangfei.android_timetableview.sample.R.layout.item_dateview, null, false);
        TextView dayTextView = v.findViewById(com.zhuangfei.android_timetableview.sample.R.id.id_week_day);
        dayTextView.setText(dateArray[pos]);
        dayTextView.setTextColor(color);

        textViews[pos] = v.findViewById(com.zhuangfei.android_timetableview.sample.R.id.id_week_date);
        layouts[pos] = v.findViewById(com.zhuangfei.android_timetableview.sample.R.id.id_week_layout);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(perDayWidth, height);
        layouts[pos].setLayoutParams(lp);
        textViews[pos].setText(weekDates.get(pos) + "日");

        return v;
    }
}
