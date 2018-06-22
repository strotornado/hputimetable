package com.zhuangfei.hputimetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class TimetableDetailActivity extends AppCompatActivity {

    private TextView nameTextView;
    private TextView roomTextView;
    private TextView weeksTextView;
    private TextView dayTextView;
    private TextView teacherTextView;

    @BindView(R.id.id_container)
    public LinearLayout container;

    private LayoutInflater inflater;

    List<Schedule> schedules;

    Class returnClass = MainActivity.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_detail);
        ButterKnife.bind(this);
        inflater = LayoutInflater.from(this);
        loadData();
    }

    private void loadData() {
        int cur_week = ShareTools.getInt(this,ShareConstants.KEY_CUR_WEEK,1);
        BundleModel model = BundleTools.getModel(this);
        if (model != null && model.get("timetable") != null) {
            schedules = (List<Schedule>) model.get("timetable");
        }
        if (model != null && model.getFromClass() != null) {
            returnClass = model.getFromClass();
        }

        List<Schedule> list = new ArrayList<>();
        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);
            if (schedule.getWeekList().indexOf(cur_week) == -1) {
                list.add(schedule);
                continue;
            }
            View view = createView(cur_week, schedule);
            container.addView(view);
        }

        for (int i = 0; i < list.size(); i++) {
            Schedule schedule = list.get(i);
            View view = createView(cur_week, schedule);
            container.addView(view);
        }
    }

    private View createView(int cur_week, Schedule schedule) {
        View view = inflater.inflate(R.layout.item_timetable_detail, null, false);
        nameTextView = (TextView) view.findViewById(R.id.id_coursedetail_name);
        roomTextView = (TextView) view.findViewById(R.id.id_coursedetail_room);
        weeksTextView = (TextView) view.findViewById(R.id.id_coursedetail_weeks);
        dayTextView = (TextView) view.findViewById(R.id.id_coursedetail_day);
        teacherTextView = (TextView) view.findViewById(R.id.id_coursedetail_teacher);

        if(schedule==null) return view;

        nameTextView.setText(schedule.getName());
        roomTextView.setText("教室\t" + schedule.getRoom());

        if (schedule.getWeekList().indexOf(cur_week) != -1) {
            nameTextView.setText(schedule.getName() + "(本周)");
        }

        String weeks="";
        if(schedule.getWeekList()!=null) weeks=schedule.getWeekList().toString();

        String day="";
        if(getDay(schedule.getDay())!=null) day=getDay(schedule.getDay());

        String teacher="";
        if(schedule.getTeacher()!=null) teacher=schedule.getTeacher();

        weeksTextView.setText("周次\t" + weeks);
        dayTextView.setText("节次\t周" + day + ",\t" + schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1) + "节");
        teacherTextView.setText("教师\t" +teacher);
        return view;
    }

    public String getDay(int day) {
        String str = "一二三四五六七";
        return str.charAt(day - 1) + "";
    }

    @OnClick(R.id.id_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, returnClass);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
