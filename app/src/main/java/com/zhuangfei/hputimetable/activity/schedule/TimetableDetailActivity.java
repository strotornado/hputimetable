package com.zhuangfei.hputimetable.activity.schedule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.schedule.AddTimetableActivity;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class TimetableDetailActivity extends AppCompatActivity {

    private TextView nameTextView;
    private TextView roomTextView;
    private TextView weeksTextView;
    private TextView dayTextView;
    private TextView teacherTextView;
    private TextView deleteTextView;
    private TextView editorTextView;

    @BindView(R.id.id_container)
    public LinearLayout container;

    private LayoutInflater inflater;

    List<Schedule> schedules;

    Class returnClass = MainActivity.class;

    int item=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_detail);
        ButterKnife.bind(this);
        inflater = LayoutInflater.from(this);
        loadData();
    }

    private void loadData() {
        int cur_week = TimetableTools.getCurWeek(this);
        BundleModel model = BundleTools.getModel(this);
        if(model==null) {
            Toasty.error(this,"参数传递错误!").show();
            goBack();
            return;
        }

        if (model != null && model.get("timetable") != null) {
            schedules = (List<Schedule>) model.get("timetable");
        }else{
            Toasty.error(this,"参数传递错误!").show();
            goBack();
            return;
        }
        if (model != null && model.getFromClass() != null) {
            returnClass = model.getFromClass();
        }
        item= (int) model.get("item",1);

        if(schedules==null){
            Toasty.error(this,"参数传递错误:schedule==null").show();
            goBack();
            return;
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

    private View createView(int cur_week, final Schedule schedule) {
        View view = inflater.inflate(R.layout.item_timetable_detail, null, false);
        nameTextView = (TextView) view.findViewById(R.id.id_coursedetail_name);
        roomTextView = (TextView) view.findViewById(R.id.id_coursedetail_room);
        weeksTextView = (TextView) view.findViewById(R.id.id_coursedetail_weeks);
        dayTextView = (TextView) view.findViewById(R.id.id_coursedetail_day);
        teacherTextView = (TextView) view.findViewById(R.id.id_coursedetail_teacher);
        deleteTextView = (TextView) view.findViewById(R.id.id_detail_delete);
        editorTextView = (TextView) view.findViewById(R.id.id_detail_editor);

        if (schedule == null) return view;

        nameTextView.setText(schedule.getName());
        roomTextView.setText(schedule.getRoom());

        if (schedule.getWeekList().indexOf(cur_week) != -1) {
            nameTextView.setText(schedule.getName() + "(本周)");
        }

        String weeks = "";
        if (schedule.getWeekList() != null) weeks = schedule.getWeekList().toString();

        String day = "";
        if (getDay(schedule.getDay()) != null) day = getDay(schedule.getDay());

        String teacher = "";
        if (schedule.getTeacher() != null) teacher = schedule.getTeacher();

        weeksTextView.setText(weeks);
        dayTextView.setText("周" + day + "    第" + schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1) + "节");
        teacherTextView.setText(teacher);

        deleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(schedule);
            }
        });
        editorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modify(schedule);
            }
        });
        return view;
    }

    public String getDay(int day) {
        String str = "一二三四五六日";
        return str.charAt(day - 1) + "";
    }

    @OnClick(R.id.id_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, returnClass, new BundleModel().put("item", item));
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void delete(Schedule schedule) {
        if (schedule != null) {
            int id = (int) schedule.getExtras().get(TimetableModel.EXTRA_ID);
            TimetableModel model = DataSupport.find(TimetableModel.class, id);
            if (model != null) {
                model.delete();
                ScheduleDao.changeFuncStatus(this,true);
                ShareTools.put(this, "course_update", 1);
                Toasty.success(this, "删除成功！").show();
                ScheduleDao.changeStatus(this,true);
                EventBus.getDefault().post(new UpdateScheduleEvent());
                BroadcastUtils.refreshAppWidget(this);
                goBack();
            }
        }
    }

    public void modify(Schedule schedule) {
        if (schedule == null) return;
        String weeks="";
        if(schedule.getWeekList()!=null){
            for(int i=0;i<schedule.getWeekList().size();i++){
                weeks+=schedule.getWeekList().get(i);
                if(i!=schedule.getWeekList().size()-1) weeks+=",";
            }
        }
        ActivityTools.toActivity(this, AddTimetableActivity.class,
                new BundleModel().setFromClass(MainActivity.class)
                        .put(AddTimetableActivity.KEY_ID, schedule.getExtras().get(TimetableModel.EXTRA_ID))
                        .put(AddTimetableActivity.KEY_TYPE, AddTimetableActivity.TYPE_MODIFY)
                        .put(AddTimetableActivity.KEY_NAME, schedule.getName())
                        .put(AddTimetableActivity.KEY_ROOM, schedule.getRoom())
                        .put(AddTimetableActivity.KEY_TEACHER, schedule.getTeacher())
                        .put(AddTimetableActivity.KEY_START, schedule.getStart())
                        .put(AddTimetableActivity.KEY_DAY, schedule.getDay())
                        .put(AddTimetableActivity.KEY_STEP, schedule.getStep())
                        .put(AddTimetableActivity.KEY_WEEKS,weeks));
    }
}
