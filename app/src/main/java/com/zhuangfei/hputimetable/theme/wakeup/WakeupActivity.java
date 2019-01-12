package com.zhuangfei.hputimetable.theme.wakeup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.view.WeekView;

import java.util.List;

public class WakeupActivity extends AppCompatActivity {

    //控件
    TimetableView mTimetableView;
    WeekView mWeekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wakeup);
        initView();
    }

    private void initView() {
        initTimetableView();
    }

    /**
     * 初始化课程控件
     */
    private void initTimetableView() {
        //获取控件
        mWeekView = findViewById(R.id.id_weekview);
        mTimetableView = findViewById(R.id.id_timetableView);

        //设置周次选择属性
        mWeekView.curWeek(1)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        mTimetableView.curWeek(1)
                .curTerm("大三下学期")
                .maxSlideItem(10)
                .monthWidthDp(30)
                .alpha(0f,0f,0.6f)
                .callback(new OnWakeupDateBuildAdapter())
                .showView();
    }
}
