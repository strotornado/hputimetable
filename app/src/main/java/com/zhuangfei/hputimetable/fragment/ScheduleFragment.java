package com.zhuangfei.hputimetable.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.AddTimetableActivity;
import com.zhuangfei.hputimetable.CustomWeekView;
import com.zhuangfei.hputimetable.MenuActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.TimetableDetailActivity;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScheduleFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private Activity context;

    @BindView(R.id.id_timetableView)
    public TimetableView mTimetableView;

    @BindView(R.id.id_weekview)
    public CustomWeekView mWeekView;

    @BindView(R.id.id_title)
    public TextView mTitleTextView;

    @BindView(R.id.id_schedulename)
    public TextView mCurScheduleTextView;

    private List<Schedule> schedules;

    public Activity getContext() {
        return context;
    }

    int target;

    @BindView(R.id.id_main_menu)
    ImageView menuImageView;

    @BindView(R.id.id_tiptext)
    TextView tipTextView;

    @BindView(R.id.container)
    LinearLayout containerLayout;

    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_schedule, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(context,view);
        inits();
        adjustAndGetData();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkData();
        mTimetableView.onDateBuildListener().onHighLight();
        int newCurWeek = TimetableTools.getCurWeek(context);
        if(newCurWeek>25) newCurWeek=25;
        if (newCurWeek != mTimetableView.curWeek()) {
            mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), newCurWeek);
            mTimetableView.changeWeekForce(newCurWeek);
            mWeekView.curWeek(newCurWeek).updateView();
        }
    }


    public void checkData() {
        int v = ShareTools.getInt(context, "course_update", 0);
        if (v == 1) {
            int id = ScheduleDao.getApplyScheduleId(context);
            ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
            mCurScheduleTextView.setText("" + scheduleName.getName());

            List<TimetableModel> dataModels = ScheduleDao.getAllWithScheduleId(id);
            if (dataModels != null) {
                mTimetableView.data(ScheduleSupport.transform(dataModels)).updateView();
                mWeekView.data(ScheduleSupport.transform(dataModels)).showView();
                if (dataModels.size() == 0) {
                    tipTextView.setVisibility(View.VISIBLE);
                } else {
                    tipTextView.setVisibility(View.GONE);
                }
            }
            ShareTools.put(context, "course_update", 0);
        }

        int changed=ShareTools.getInt(context,"hidenotcur_changed",0);
        if(changed==1){
            int status=ShareTools.getInt(context,"hidenotcur",0);
            if(status==0){
                mTimetableView.isShowNotCurWeek(true).updateView();;
            }else {
                mTimetableView.isShowNotCurWeek(false).updateView();
            }
            ShareTools.putInt(context, "hidenotcur_changed", 0);
        }

        int mainThemechanged=ShareTools.getInt(context,"mainalpha_changed",0);
        if(mainThemechanged==1){
            int status=ShareTools.getInt(context,"mainalpha",0);
            if(status==0){
                setWhiteBg(true);
            }else {
                setTransportBg(true);
            }
            ShareTools.putInt(context, "mainalpha_changed", 0);
        }
    }

    public void setWhiteBg(boolean isUpdate){
        mTitleTextView.setTextColor(context.getResources().getColor(R.color.app_course_textcolor_blue));
        menuImageView.setColorFilter(Color.GRAY);
        mCurScheduleTextView.setTextColor(Color.GRAY);
        containerLayout.setBackgroundColor(Color.WHITE);
        if(isUpdate){
            mTimetableView.alpha(1).updateView();
        }
    }

    public void setTransportBg(boolean isUpdate){
        mTitleTextView.setTextColor(Color.WHITE);
        menuImageView.setColorFilter(Color.WHITE);
        mCurScheduleTextView.setTextColor(Color.WHITE);
        containerLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.main_bg));
        if(isUpdate){
            mTimetableView.alpha(0.2f,0.05f,0.75f).updateView();
        }
    }

    private void inits() {
        context = getActivity();
        menuImageView.setColorFilter(Color.WHITE);
        schedules = new ArrayList<>();

        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if (scheduleName != null) {
            mCurScheduleTextView.setText("" + scheduleName.getName());
        } else {
            mCurScheduleTextView.setText("默认课表");
        }

        int curWeek = TimetableTools.getCurWeek(context);

        if(curWeek>=25) curWeek=25;

        //设置周次选择属性
        mWeekView.data(schedules)
                .curWeek(curWeek)
                .itemCount(25)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int curWeek) {
                        if (mTimetableView.dataSource() != null) {
                            mTimetableView.changeWeekOnly(curWeek);
                        }
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false);
        mWeekView.showView();

        int status=ShareTools.getInt(context,"hidenotcur",0);
        if(status==0){
            mTimetableView.isShowNotCurWeek(true);
        }else {
            mTimetableView.isShowNotCurWeek(false);
        }

        int status2=ShareTools.getInt(context,"mainalpha",0);
        float alpha1,alpha2,alpha3;
        if(status2==0){
            setWhiteBg(false);
            alpha1=1f;
            alpha2=1f;
            alpha3=1f;
        }else {
            setTransportBg(false);
            alpha1=0.2f;
            alpha2=0.05f;
            alpha3=0.75f;
        }

        mTimetableView.curWeek(curWeek)
                .maxSlideItem(10)
                .alpha(alpha1,alpha2,alpha3)
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(ScheduleFragment.class);
                        ActivityTools.toActivity(getContext(), TimetableDetailActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), curWeek);
                        String text = "第" + curWeek + "周";
                        mTitleTextView.setText(text);
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        BundleModel model = new BundleModel();
                        model.setFromClass(ScheduleFragment.class)
                                .put(AddTimetableActivity.KEY_DAY, day)
                                .put(AddTimetableActivity.KEY_START, start);
                        ActivityTools.toActivity(getContext(), AddTimetableActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnFlaglayoutClickListener() {
                    @Override
                    public void onFlaglayoutClick(int day, int start) {
                        mTimetableView.hideFlaglayout();
                        BundleModel model = new BundleModel();
                        model.setFromClass(ScheduleFragment.class)
                                .put(AddTimetableActivity.KEY_DAY, day + 1)
                                .put(AddTimetableActivity.KEY_START, start);
                        ActivityTools.toActivity(getContext(), AddTimetableActivity.class, model);
                    }
                })
                .showView();
    }

    /**
     * 周次选择布局的左侧被点击时回调
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[25];
        for (int i = 0; i < 25; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: " + which);
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mWeekView.scrollToIndex(target);
                    mTimetableView.changeWeekForce(target + 1);
                    ShareTools.putString(getContext(), ShareConstants.STRING_START_TIME, TimetableTools.getStartSchoolTime(target + 1));
                    BroadcastUtils.refreshAppWidget(context);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void adjustAndGetData() {
        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            id = scheduleName.getId();
            ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);
        }
        List<TimetableModel> dataModels = ScheduleDao.getAllWithScheduleId(id);
        if (dataModels != null) {
            mTimetableView.data(ScheduleSupport.transform(dataModels)).updateView();
            mWeekView.data(ScheduleSupport.transform(dataModels)).showView();

            if (dataModels.size() == 0) {
                tipTextView.setVisibility(View.VISIBLE);
            } else {
                tipTextView.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.id_main_menu)
    public void toMenuActivity() {
        ActivityTools.toActivity(getContext(), MenuActivity.class);
    }

    @OnClick(R.id.id_layout)
    public void onTitleClicked() {
        if (mWeekView.isShowing()) {
            mWeekView.isShow(false);
            mTimetableView.changeWeekForce(mTimetableView.curWeek());
        } else {
            mWeekView.isShow(true);
            mWeekView.scrollToIndex(mTimetableView.curWeek() - 1);
        }
    }
}
