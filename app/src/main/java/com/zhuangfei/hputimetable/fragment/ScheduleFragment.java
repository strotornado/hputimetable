package com.zhuangfei.hputimetable.fragment;

import android.app.Activity;
import android.content.Context;
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
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.listener.OnTitleClickedListener;
import com.zhuangfei.hputimetable.listener.OnStatusChangedListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ScheduleFragment extends Fragment implements OnTitleClickedListener,OnSwitchTableListener {

    private static final String TAG = "MainActivity";
    private Activity context;

    @BindView(R.id.id_timetableView)
    public TimetableView mTimetableView;

    @BindView(R.id.id_weekview)
    public CustomWeekView mWeekView;

    private List<Schedule> schedules;

    public Activity getContext() {
        return context;
    }

    int target;

    OnStatusChangedListener onWeekChangedListener;

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
        ButterKnife.bind(this,view);
        inits();
        adjustAndGetData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnStatusChangedListener){
            onWeekChangedListener= (OnStatusChangedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onWeekChangedListener=null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimetableView.onDateBuildListener().onHighLight();
        int newCurWeek = TimetableTools.getCurWeek(context);
        if(newCurWeek != mTimetableView.curWeek()) {
            mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), newCurWeek);
            mTimetableView.changeWeekForce(newCurWeek);
            mWeekView.curWeek(newCurWeek).updateView();
        }
    }

    private void inits() {
        context = getActivity();
        schedules = new ArrayList<>();

        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if (scheduleName != null) {
            onWeekChangedListener.onScheduleNameChanged("" + scheduleName.getName());
        } else {
            onWeekChangedListener.onScheduleNameChanged("默认课表");
        }

        int curWeek = TimetableTools.getCurWeek(context);

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

        int status2=ShareTools.getInt(context,"hideweekends",0);
        if(status2==0){
            mTimetableView.isShowWeekends(true);
        }else {
            mTimetableView.isShowWeekends(false);
        }

        mTimetableView.curWeek(curWeek)
                .maxSlideItem(10)
                .itemHeight(ScreenUtils.dip2px(context,50))
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(getActivity().getClass());
                        ActivityTools.toActivity(getContext(), TimetableDetailActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), curWeek);
                        if(onWeekChangedListener!=null){
                            onWeekChangedListener.onWeekChanged(curWeek);
                        }
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        BundleModel model = new BundleModel();
                        model.setFromClass(getActivity().getClass())
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
                        model.setFromClass(getActivity().getClass())
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

    @Override
    public void onTitleClick() {
        if (mWeekView.isShowing()) {
            mWeekView.isShow(false);
            mTimetableView.changeWeekForce(mTimetableView.curWeek());
        } else {
            mWeekView.isShow(true);
            mWeekView.scrollToIndex(mTimetableView.curWeek() - 1);
        }
    }

    @Override
    public void onSwitchTable(ScheduleName scheduleName) {
        if (scheduleName == null) return;
        int id = scheduleName.getId();
        ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);

        onWeekChangedListener.onScheduleNameChanged("" + scheduleName.getName());
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
        Toasty.success(context, "切换课表成功").show();
    }
}
