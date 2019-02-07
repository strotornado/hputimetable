package com.zhuangfei.hputimetable.activity.debug;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.timetable_custom.CustomWeekView;
import com.zhuangfei.hputimetable.activity.schedule.TimetableDetailActivity;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebugDisplayActivity extends AppCompatActivity {

    private Activity context;

    @BindView(R.id.id_timetableView)
    public TimetableView mTimetableView;

    @BindView(R.id.id_weekview)
    public CustomWeekView mWeekView;

    private List<Schedule> schedules;

    public Activity getContext() {
        return context;
    }

    @BindView(R.id.id_title)
    public TextView mTitleTextView;

    @BindView(R.id.id_main_menu)
    ImageView menuImageView;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    int tmp=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_schedule);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        menuImageView.setColorFilter(Color.WHITE);
        menuImageView.setVisibility(View.VISIBLE);
        menuImageView.setVisibility(View.GONE);
        context =this;
        schedules = new ArrayList<>();
        List<Schedule> list= (List<Schedule>) getIntent().getSerializableExtra("schedules");
        if(list!=null){
            schedules=list;
        }
        int curWeek = TimetableTools.getCurWeek(context);
        tmp=curWeek;

        //设置周次选择属性
        mWeekView.data(schedules)
                .curWeek(curWeek)
                .itemCount(25)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        tmp=week;
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)
                .showView();

        mTimetableView.data(schedules)
                .curWeek(curWeek)
                .maxSlideItem(10)
                .itemHeight(ScreenUtils.dip2px(context,50))
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(DebugDisplayActivity.class);
                        ActivityTools.toActivityWithout(getContext(), TimetableDetailActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        mTitleTextView.setText("第"+curWeek+"周");
                        tmp=curWeek;
                    }
                })
                .showView();
        loadLayout.setVisibility(View.GONE);
    }

    /**
     * 周次选择布局的左侧被点击时回调
     */
    protected void onWeekLeftLayoutClicked() {
        Toast.makeText(this,"这里仅仅是模拟哟，不要当真!",Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.id_layout)
    public void onTitleClick() {
        if (mWeekView.isShowing()) {
            mWeekView.isShow(false);
            mTimetableView.changeWeekForce(mTimetableView.curWeek());
            mTimetableView.onDateBuildListener().onUpdateDate(tmp,mTimetableView.curWeek());
        } else {
            mWeekView.isShow(true);
            mWeekView.scrollToIndex(mTimetableView.curWeek() - 1);
        }
    }
}
