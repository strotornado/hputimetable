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
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.activity.schedule.AddTimetableActivity;
import com.zhuangfei.hputimetable.event.ConfigChangeEvent;
import com.zhuangfei.hputimetable.timetable_custom.CustomWeekView;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.schedule.TimetableDetailActivity;
import com.zhuangfei.hputimetable.adapter.OnGryphonConfigHandler;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.theme.IThemeView;
import com.zhuangfei.hputimetable.theme.MyThemeLoader;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ScheduleFragment extends LazyLoadFragment implements IThemeView{

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

    @BindView(R.id.container)
    LinearLayout containerLayout;

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

    int tmp=1;

    MyThemeLoader mThemeLoader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_schedule, container, false);
        EventBus.getDefault().register(this);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this,view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        inits();
        adjustAndGetData();
    }

    private void inits() {
        menuImageView.setColorFilter(Color.WHITE);
        menuImageView.setVisibility(View.VISIBLE);
        context = getActivity();
        schedules = new ArrayList<>();
        mThemeLoader=new MyThemeLoader(this);

        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if (scheduleName != null) {
            mCurScheduleTextView.setText(scheduleName.getName());
        } else {
            mCurScheduleTextView.setText("默认课表");
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

        mTimetableView.curWeek(curWeek)
                .maxSlideItem(10)
                .configName(MenuActivity.defaultConfigName)
                .itemHeight(ScreenUtils.dip2px(context,50))
//                .callback(new CalenderDateBuildAdapter(context))
                .callback(new OnGryphonConfigHandler())
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(getActivity().getClass());
                        model.put("item",1);
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

        if(scheduleName==null) return;
        ScheduleName newName = DataSupport.find(ScheduleName.class, id);
        if(newName==null) return;
        FindMultiExecutor executor=newName.getModelsAsync();
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
    public void showPopMenu(){
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(context, menuImageView);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.id_menu2:
                        ActivityTools.toActivityWithout(context,AddTimetableActivity.class);
                        break;
                }
                return false;
            }
        });
        popup.show(); //这一行代码不要忘记了
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChangeEvent(ConfigChangeEvent event){
        mTimetableView.updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateScheduleEvent(UpdateScheduleEvent event){
        ScheduleName newName = DataSupport.find(ScheduleName.class, ScheduleDao.getApplyScheduleId(getActivity()));
        if(newName==null) return;
        final int curWeek = TimetableTools.getCurWeek(context);
        mCurScheduleTextView.setText(newName.getName());
        FindMultiExecutor executor=newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> dataModels = (List<TimetableModel>) t;
                if (dataModels != null) {
                    mTimetableView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).updateView();
                    mWeekView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).showView();
                }
            }
        });
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
}
