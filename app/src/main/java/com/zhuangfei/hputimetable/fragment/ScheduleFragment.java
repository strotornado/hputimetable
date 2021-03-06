package com.zhuangfei.hputimetable.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.bugly.crashreport.BuglyLog;
import com.zhuangfei.hputimetable.activity.schedule.AddTimetableActivity;
import com.zhuangfei.hputimetable.event.ConfigChangeEvent;
import com.zhuangfei.hputimetable.event.UpdateTabTextEvent;
import com.zhuangfei.hputimetable.timetable_custom.CustomWeekView;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.schedule.TimetableDetailActivity;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.theme.IThemeView;
import com.zhuangfei.hputimetable.theme.MyThemeLoader;
import com.zhuangfei.hputimetable.timetable_custom.OnSundayFirstDateBuildAdapter;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.listener.OnDateBuildAapter;
import com.zhuangfei.timetable.listener.OnScrollViewBuildAdapter;
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleConfig;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ScheduleFragment extends LazyLoadFragment implements IThemeView {

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

    private View mView;

    @BindView(R.id.id_title)
    public TextView mTitleTextView;

    @BindView(R.id.id_schedulename)
    public TextView mCurScheduleTextView;

    @BindView(R.id.id_main_menu)
    ImageView menuImageView;

    public static final int REQUEST_IMPORT = 1;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    int tmp = 1;

    MyThemeLoader mThemeLoader;

    @BindView(R.id.statuslayout)
    View statusView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_schedule, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        try {
            int statusHeight = ViewTools.getStatusHeight(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight);
            statusView.setLayoutParams(lp);
        } catch (Exception e) {
            BuglyLog.e("FuncFragment", "onViewCreated", e);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        inits();
        adjustAndGetData();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, 300);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                try {
                    mTimetableView.onDateBuildListener().onHighLight();
                    int newCurWeek = TimetableTools.getCurWeek(context);
                    if (newCurWeek != mTimetableView.curWeek()) {
                        mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), newCurWeek);
                        mTimetableView.changeWeekForce(newCurWeek);
                        mWeekView.curWeek(newCurWeek).updateView();
                    }
                } catch (Exception e) {
                }
            }
        }
    };

    private void inits() {
        menuImageView.setColorFilter(Color.WHITE);
        menuImageView.setVisibility(View.VISIBLE);
        context = getActivity();
        schedules = new ArrayList<>();
        mThemeLoader = new MyThemeLoader(this);

        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if (scheduleName != null) {
            mCurScheduleTextView.setText(scheduleName.getName());
        } else {
            mCurScheduleTextView.setText("默认课表");
        }

        int curWeek = TimetableTools.getCurWeek(context);
        tmp = curWeek;


        //设置周次选择属性
        mWeekView.data(schedules)
                .curWeek(curWeek)
                .itemCount(25)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        tmp = week;
                        EventBus.getDefault().post(new UpdateTabTextEvent("第" + week + "周"));
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

        int status = ShareTools.getInt(context, "hidenotcur", 0);
        if (status == 0) {
            mTimetableView.isShowNotCurWeek(true);
        } else {
            mTimetableView.isShowNotCurWeek(false);
        }

        int status2 = ShareTools.getInt(context, "hideweekends", 0);
        if (status2 == 0) {
            mTimetableView.isShowWeekends(true);
        } else {
            mTimetableView.isShowWeekends(false);
        }

        int isFirstSunday = ShareTools.getInt(getActivity(), "isFirstSunday", 0);
        if (isFirstSunday == 1) {
            mTimetableView.callback(new OnSundayFirstDateBuildAdapter())
                    .callback(new ISchedule.OnScrollViewBuildListener() {
                        @Override
                        public View getScrollView(LayoutInflater mInflate) {
                            return mInflate.inflate(R.layout.view_firstsunday_scrollview, null);
                        }
                    });
        } else {
            mTimetableView.callback((ISchedule.OnDateBuildListener) null)
                    .callback((ISchedule.OnScrollViewBuildListener) null);
        }

        int maxCount = ShareTools.getInt(context, "maxCount", 12);
        mTimetableView.maxSlideItem(maxCount);

        final List<String> startTimeList=new ArrayList<>();
        final List<String> endTimeList=new ArrayList<>();
        TimetableTools.getTimeList(getContext(),startTimeList,endTimeList);
        OnSlideBuildAdapter slideBuildAdapter=new OnSlideBuildAdapter();
        String time= ShareTools.getString(getContext(),"schedule_time",null);
        if(!TextUtils.isEmpty(time)){
            String[] times= new String[startTimeList.size()];
            for(int i=0;i<startTimeList.size();i++){
                times[i]=startTimeList.get(i);
            }
            slideBuildAdapter.setTimes(times);
        }

        mTimetableView.curWeek(curWeek)
                .itemHeight(ScreenUtils.dip2px(context, 50))
//                .callback(new CalenderDateBuildAdapter(context))
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(getActivity().getClass());
                        model.put("item", 1);
                        ActivityTools.toActivityWithout(getContext(), TimetableDetailActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        EventBus.getDefault().post(new UpdateTabTextEvent("第" + curWeek + "周"));
                        mTitleTextView.setText("第" + curWeek + "周");
                        tmp = curWeek;
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        BundleModel model = new BundleModel();
                        model.setFromClass(getActivity().getClass())
                                .put(AddTimetableActivity.KEY_DAY, day)
                                .put(AddTimetableActivity.KEY_START, start);
                        ActivityTools.toActivityWithout(getContext(), AddTimetableActivity.class, model);
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
                        ActivityTools.toActivityWithout(getContext(), AddTimetableActivity.class, model);
                    }
                })
                .callback(slideBuildAdapter)
                .showView();
        loadLayout.setVisibility(View.GONE);
        mThemeLoader.execute();
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
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mWeekView.scrollToIndex(target);
                    mTimetableView.changeWeekForce(target + 1);
                    ShareTools.putString(getContext(), ShareConstants.STRING_START_TIME, TimetableTools.getStartSchoolTime(target + 1));
                    BroadcastUtils.refreshAppWidget(context);
                    EventBus.getDefault().post(new UpdateScheduleEvent());
                    ToastTools.show(getContext(), "当前周:" + (target + 1) + "\n开学时间:" + TimetableTools.getStartSchoolTime(target + 1));
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

        if (scheduleName == null) return;
        ScheduleName newName = DataSupport.find(ScheduleName.class, id);
        if (newName == null) return;
        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> dataModels = (List<TimetableModel>) t;
                if (dataModels != null) {
                    mTimetableView.data(ScheduleSupport.transform(dataModels)).updateView();
                    mWeekView.data(ScheduleSupport.transform(dataModels)).showView();
                }
            }
        });
    }

    @OnClick(R.id.id_main_menu)
    public void showPopMenu() {
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(context, menuImageView);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.id_menu2:
                        ActivityTools.toActivityWithout(context, AddTimetableActivity.class);
                        break;
                    case R.id.id_menu3:
                        mWeekView.isShow(true);
                        break;
                    case R.id.id_menu4:
                        mWeekView.isShow(false);
                        break;
                }
                return false;
            }
        });
        popup.show(); //这一行代码不要忘记了
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChangeEvent(ConfigChangeEvent event) {
        int status = ShareTools.getInt(context, "hidenotcur", 0);
        if (status == 0) {
            mTimetableView.isShowNotCurWeek(true);
        } else {
            mTimetableView.isShowNotCurWeek(false);
        }

        int status2 = ShareTools.getInt(context, "hideweekends", 0);
        if (status2 == 0) {
            mTimetableView.isShowWeekends(true);
        } else {
            mTimetableView.isShowWeekends(false);
        }

        int maxCount = ShareTools.getInt(context, "maxCount", 10);
        mTimetableView.maxSlideItem(maxCount);

        changeConfig();

        final List<String> startTimeList=new ArrayList<>();
        final List<String> endTimeList=new ArrayList<>();
        TimetableTools.getTimeList(getContext(),startTimeList,endTimeList);
        OnSlideBuildAdapter slideBuildAdapter=new OnSlideBuildAdapter();
        String time= ShareTools.getString(getContext(),"schedule_time",null);
        if(!TextUtils.isEmpty(time)){
            String[] times= new String[startTimeList.size()];
            for(int i=0;i<startTimeList.size();i++){
                times[i]=startTimeList.get(i);
            }
            slideBuildAdapter.setTimes(times);
        }

        int curWeek = TimetableTools.getCurWeek(getActivity());
        mTimetableView.curWeek(curWeek).updateView();
        mWeekView.curWeek(curWeek).showView();
    }

    public void changeConfig() {
        int isFirstSunday = ShareTools.getInt(getActivity(), "isFirstSunday", 0);
        if (isFirstSunday == 1) {
            mTimetableView.callback(new OnSundayFirstDateBuildAdapter())
                    .callback(new ISchedule.OnScrollViewBuildListener() {
                        @Override
                        public View getScrollView(LayoutInflater mInflate) {
                            return mInflate.inflate(R.layout.view_firstsunday_scrollview, null);
                        }
                    });
        } else {
            mTimetableView.callback(new OnDateBuildAapter())
                    .callback(new OnScrollViewBuildAdapter());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateScheduleEvent(UpdateScheduleEvent event) {
        ScheduleName newName = DataSupport.find(ScheduleName.class, ScheduleDao.getApplyScheduleId(getActivity()));
        if (newName == null) return;
        final int curWeek = TimetableTools.getCurWeek(context);
        UpdateTabTextEvent updateTabTextEvent = new UpdateTabTextEvent();
        updateTabTextEvent.setText("第" + curWeek + "周");
        EventBus.getDefault().post(updateTabTextEvent);
        mCurScheduleTextView.setText(newName.getName());
        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> dataModels = (List<TimetableModel>) t;
                if (dataModels != null) {
                    changeConfig();
                    mTimetableView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).updateView();
                    mWeekView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).showView();
                }
            }
        });
    }

    public void onToggleWeekViewEvent() {
        if (mWeekView.isShowing()) {
            mWeekView.isShow(false);
            mTimetableView.changeWeekForce(mTimetableView.curWeek());
            mTimetableView.onDateBuildListener().onUpdateDate(tmp, mTimetableView.curWeek());
        } else {
            mWeekView.isShow(true);
            mWeekView.scrollToIndex(mTimetableView.curWeek() - 1);
        }
    }

    @Override
    public TimetableView getTimetableView() {
        return mTimetableView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.id_title)
    public void onCurWeekTextClicked() {
        onToggleWeekViewEvent();
    }
}
