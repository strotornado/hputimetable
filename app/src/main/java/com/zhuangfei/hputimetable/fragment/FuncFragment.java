package com.zhuangfei.hputimetable.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tencent.bugly.crashreport.BuglyLog;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.CustomGridView;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.activity.AddTodoActivity;
import com.zhuangfei.hputimetable.activity.LoginActivity;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.activity.MessageActivity;
import com.zhuangfei.hputimetable.activity.VipActivity;
import com.zhuangfei.hputimetable.activity.adapter.SearchSchoolActivity;
import com.zhuangfei.hputimetable.activity.schedule.MultiScheduleActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.ScanActivity;
import com.zhuangfei.hputimetable.activity.schedule.TimetableDetailActivity;
import com.zhuangfei.hputimetable.adapter.StationAdapter;
import com.zhuangfei.hputimetable.adapter_apis.AssetTools;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.GreenFruitProfile;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TodoModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.ReloadStationEvent;
import com.zhuangfei.hputimetable.event.SwitchPagerEvent;
import com.zhuangfei.hputimetable.event.UpdateBindDataEvent;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.event.UpdateStationHomeEvent;
import com.zhuangfei.hputimetable.event.UpdateTodoEvent;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.ImportTools;
import com.zhuangfei.hputimetable.tools.PermissionTools;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleColorPool;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;
import com.zhuangfei.toolkit.widget.listener.OnBaseScrollViewListener;
import com.zhuangfei.toolkit.widget.view.BaseScrollView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Administrator 刘壮飞
 */
@SuppressLint({"NewApi", "ValidFragment"})
public class FuncFragment extends LazyLoadFragment {

    private View mView;

    @BindView(R.id.id_cardview_layout)
    LinearLayout cardLayout;

    @BindView(R.id.id_cardview_today)
    TextView todayInfo;

    @BindView(R.id.id_func_schedulename)
    TextView scheduleNameText;

    @BindView(R.id.id_cardview_layout2)
    LinearLayout cardLayout2;

    @BindView(R.id.id_func_message_count)
    TextView messageCountView;

    SharedPreferences messagePreferences;

    @BindView(R.id.id_func_gridview)
    CustomGridView stationGridView;
    List<StationModel> stationModels;
    StationAdapter stationAdapter;

    @BindView(R.id.id_bind_course)
    LinearLayout isBindLayout;

    @BindView(R.id.id_ta_layout)
    LinearLayout bindContainer;

    @BindView(R.id.id_vip_layout)
    LinearLayout vipLayout;

    @BindView(R.id.id_help_layout)
    LinearLayout helpLayout;


    @BindView(R.id.id_func_setting_img)
    ImageView settingsImageView;

    @BindView(R.id.statuslayout)
    View statusView;

    @BindView(R.id.id_cardview_todolayout)
    LinearLayout todoCardLayout;

    @BindView(R.id.id_todo)
    LinearLayout todoContainerLayout;

    int curWeek = 1;
    int dayOfWeek = -1;

    boolean isInit = false;

    boolean isEdit = false;

    List<TextView> deleteTextList = null;
    List<TextView> timeTextList = null;

    @BindView(R.id.id_edittodo)
    TextView editTodoText;

    Typeface normalTypeFace = null;

    final int SUCCESSCODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_func, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
        isInit = true;
        inits();
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
            if (msg.what == 0x123 && isInit) {
                try {
                    int newCurWeek = TimetableTools.getCurWeek(getContext());
                    int newDayOfWeek = getDayOfWeek();
                    if (newCurWeek != curWeek || newDayOfWeek != dayOfWeek) {
                        findData();
                    }
                    getUnreadMessageCount();
                } catch (Exception e) {
                }
            }
        }
    };

    private void inits() {
//        createDayViewBottom();
        settingsImageView.setColorFilter(getResources().getColor(R.color.app_gray));
        messagePreferences = getContext().getSharedPreferences("app_message", Context.MODE_PRIVATE);
        stationModels = new ArrayList<>();
        stationAdapter = new StationAdapter(getContext(), stationModels);
        stationGridView.setAdapter(stationAdapter);
        stationGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (stationModels.size() > i) {
                    StationManager.openStationWithout(getActivity(), stationModels.get(i));
                }
            }
        });
        //todo
        int showVip = ShareTools.getInt(getActivity(), ShareConstants.INT_VIP_LAYOUT, 1);
        if (showVip == 1 && !VipTools.isVip(getActivity()).isSuccess()) {
            vipLayout.setVisibility(View.VISIBLE);
        }

        int showHelp = ShareTools.getInt(getContext(), "showHelpLayout", 1);
        if (showHelp == 1) {
            helpLayout.setVisibility(View.VISIBLE);
        }
        int isShowTodo = ShareTools.getInt(getContext(), ShareConstants.INT_TODO_LAYOUT, 1);
        if (isShowTodo == 1) {
            todoContainerLayout.setVisibility(View.VISIBLE);
        } else {
            todoContainerLayout.setVisibility(View.GONE);
        }
        normalTypeFace = editTodoText.getTypeface();
        registerForContextMenu(stationGridView);
        findData();
        findTodoData();
        findStationLocal();
        getUnreadMessageCount();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(1, 1, 1, "从主页删除");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        StationModel stationModel = stationModels.get(info.position);
        switch (item.getItemId()) {
            case 1:
                DataSupport.delete(StationModel.class, stationModel.getId());
                findStationLocal();
                ToastTools.show(getContext(), "已从主页删除");
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void createCardView(List<Schedule> models, ScheduleName newName) {
        if (getContext() == null) return;
        ScheduleColorPool colorPool = new ScheduleColorPool(getContext());
        cardLayout.removeAllViews();
        SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE");
        int curWeek = TimetableTools.getCurWeek(getActivity());
        todayInfo.setText("第" + curWeek + "周  " + sdf2.format(new Date()));

        if (newName != null) {
            scheduleNameText.setText(newName.getName());
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        if (models == null) {
            View view = inflater.inflate(R.layout.item_empty, null, false);
            TextView infoText = view.findViewById(R.id.item_empty);
            TextView infoButtonText = view.findViewById(R.id.item_to_station);
            view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new SwitchPagerEvent());
                }
            });
            infoButtonText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImportDialog();
                }
            });
            infoButtonText.setText("导入课程");
            infoText.setText("本地没有数据,去添加!");
            cardLayout.addView(view);
        } else if (models.size() == 0) {
            View view = inflater.inflate(R.layout.item_empty, null, false);
            TextView infoButtonText = view.findViewById(R.id.item_to_station);
            TextView infoText = view.findViewById(R.id.item_empty);
            view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new SwitchPagerEvent());
                }
            });
            infoButtonText.setVisibility(View.GONE);
            cardLayout.addView(view);

        } else {
            final List<String> startTimeList = new ArrayList<>();
            final List<String> endTimeList = new ArrayList<>();
            boolean isGetTime = TimetableTools.getTimeList(getContext(), startTimeList, endTimeList);
            String time = ShareTools.getString(getContext(), "schedule_time", null);

            for (int i = 0; i < models.size(); i++) {
                final Schedule schedule = models.get(i);
                if (schedule == null) continue;
                View view = inflater.inflate(R.layout.item_cardview, null, false);
                TextView startText = view.findViewById(R.id.id_item_start);
                TextView nameText = view.findViewById(R.id.id_item_name);
                TextView roomText = view.findViewById(R.id.id_item_room);
                View colorView = view.findViewById(R.id.id_item_color);
                colorView.setVisibility(View.GONE);
                colorView.setBackgroundColor(colorPool.getColorAuto(schedule.getColorRandom()));

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(colorPool.getColorAuto(schedule.getColorRandom()));
                gd.setCornerRadius(ScreenUtils.dip2px(getContext(), 3));
                startText.setBackgroundDrawable(gd);

                String name = schedule.getName();
                String room = schedule.getRoom();
                if (TextUtils.isEmpty(name)) name = "课程名未知";
                if (TextUtils.isEmpty(room)) room = "上课地点未知";
                if (!TextUtils.isEmpty(time)) {
                    int startTimeIndex = schedule.getStart() - 1;
                    int endTimeIndex = schedule.getStart() + schedule.getStep() - 2;
                    if (startTimeIndex < startTimeList.size() && endTimeIndex < endTimeList.size()) {
                        room = startTimeList.get(startTimeIndex) + "-" + endTimeList.get(endTimeIndex) + " | " + room;
                    }
                }
                nameText.setText(name);
                roomText.setText(room);
                startText.setText(schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1) + "节");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Schedule> list = new ArrayList<>();
                        list.add(schedule);
                        BundleModel model = new BundleModel();
                        model.put("timetable", list);
                        model.setFromClass(getActivity().getClass());
                        model.put("item", 0);
                        ActivityTools.toActivityWithout(getActivity(), TimetableDetailActivity.class, model);
                    }
                });
                cardLayout.addView(view);
            }
        }
    }

    public void createCardView2(List<Schedule> models, ScheduleName newName) {
        if (getContext() == null) return;
        ScheduleColorPool colorPool = new ScheduleColorPool(getContext());
        cardLayout2.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        if (models == null || models.size() == 0) {
            View view = inflater.inflate(R.layout.item_empty, null, false);
            TextView infoButtonText = view.findViewById(R.id.item_to_station);
            TextView infoText = view.findViewById(R.id.item_empty);
            infoButtonText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toSearchSchool();
                }
            });
            infoButtonText.setVisibility(View.GONE);
            cardLayout2.addView(view);
        } else {
            final List<String> startTimeList = new ArrayList<>();
            final List<String> endTimeList = new ArrayList<>();
            TimetableTools.getTimeList(getContext(), startTimeList, endTimeList);
            String time = ShareTools.getString(getContext(), "schedule_time", null);

            for (int i = 0; i < models.size(); i++) {
                final Schedule schedule = models.get(i);
                if (schedule == null) continue;
                View view = inflater.inflate(R.layout.item_cardview, null, false);
                TextView startText = view.findViewById(R.id.id_item_start);
                TextView nameText = view.findViewById(R.id.id_item_name);
                TextView roomText = view.findViewById(R.id.id_item_room);
                View colorView = view.findViewById(R.id.id_item_color);
                colorView.setVisibility(View.GONE);
                colorView.setBackgroundColor(colorPool.getColorAuto(schedule.getColorRandom()));

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(colorPool.getColorAuto(schedule.getColorRandom()));
                gd.setCornerRadius(ScreenUtils.dip2px(getContext(), 3));
                startText.setBackgroundDrawable(gd);

                String name = schedule.getName();
                String room = schedule.getRoom();
                if (TextUtils.isEmpty(name)) name = "课程名未知";
                if (TextUtils.isEmpty(room)) room = "上课地点未知";
                if (!TextUtils.isEmpty(time)) {
                    int startTimeIndex = schedule.getStart() - 1;
                    int endTimeIndex = schedule.getStart() + schedule.getStep() - 2;
                    if (startTimeIndex < startTimeList.size() && endTimeIndex < endTimeList.size()) {
                        room = startTimeList.get(startTimeIndex) + "-" + endTimeList.get(endTimeIndex) + " | " + room;
                    }
                }
                nameText.setText(name);
                roomText.setText(room);
                startText.setText(schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1) + "节");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Schedule> list = new ArrayList<>();
                        list.add(schedule);
                        BundleModel model = new BundleModel();
                        model.put("timetable", list);
                        model.setFromClass(getActivity().getClass());
                        model.put("item", 0);
                        ActivityTools.toActivityWithout(getActivity(), TimetableDetailActivity.class, model);
                    }
                });
                cardLayout2.addView(view);
            }
        }
    }

    @OnClick(R.id.id_search_school)
    public void toSearchSchool() {
        ActivityTools.toActivityWithout(getActivity(), SearchSchoolActivity.class);
    }

    /**
     * 获取数据
     *
     * @return
     */
    public void findData() {
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            ShareTools.put(getActivity(), ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
        }

        int id = ScheduleDao.getApplyScheduleId(getActivity());
        final ScheduleName newName = DataSupport.find(ScheduleName.class, id);
        if (newName == null) return;

        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> models = (List<TimetableModel>) t;
                if (models != null) {
                    List<Schedule> allModels = ScheduleSupport.transform(models);
                    if (allModels != null && allModels.size() != 0) {
                        curWeek = TimetableTools.getCurWeek(getActivity());
                        dayOfWeek = getDayOfWeek();
                        List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
                        list = ScheduleSupport.getColorReflect(list);
                        if (list == null) list = new ArrayList<>();
                        createCardView(list, newName);
                    } else createCardView(null, newName);
                }
            }
        });
        findData2();
    }

    /**
     * 获取数据
     *
     * @return
     */
    public void findTodoData() {
        FindMultiExecutor executor = DataSupport.findAllAsync(TodoModel.class);
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TodoModel> models = (List<TodoModel>) t;
                if (models != null) {
                    createTodoCardView(models);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void findTodoDataEvent(UpdateTodoEvent event) {
        int isShowTodo = ShareTools.getInt(getContext(), ShareConstants.INT_TODO_LAYOUT, 1);
        if (isShowTodo == 1) {
            todoContainerLayout.setVisibility(View.VISIBLE);
        } else {
            todoContainerLayout.setVisibility(View.GONE);
        }
        findTodoData();
    }

    public void createTodoCardView(List<TodoModel> models) {
        if (getContext() == null) return;
        todoCardLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        deleteTextList = new ArrayList<>();
        timeTextList = new ArrayList<>();

        if (models == null || models.size() == 0) {
            editTodoText.setText("编辑");
            editTodoText.setTextColor(Color.BLACK);
            isEdit = false;
            for (TextView delete : deleteTextList) {
                if (delete != null) {
                    delete.setVisibility(View.GONE);
                }
            }
            for (TextView time : timeTextList) {
                if (time != null) {
                    time.setVisibility(View.VISIBLE);
                }
            }
        }

        if (models == null) {
//            View view = inflater.inflate(R.layout.item_empty, null, false);
//            TextView infoText = view.findViewById(R.id.item_empty);
//            TextView infoButtonText = view.findViewById(R.id.item_to_station);
//            view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    EventBus.getDefault().post(new SwitchPagerEvent());
//                }
//            });
//            infoButtonText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    showImportDialog();
//                }
//            });
//            infoButtonText.setText("导入课程");
//            infoText.setText("本地没有数据,去添加!");
//            cardLayout.addView(view);
        } else if (models.size() == 0) {
//            View view = inflater.inflate(R.layout.item_empty, null, false);
//            TextView infoButtonText = view.findViewById(R.id.item_to_station);
//            TextView infoText = view.findViewById(R.id.item_empty);
//            view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    EventBus.getDefault().post(new SwitchPagerEvent());
//                }
//            });
//            infoButtonText.setVisibility(View.GONE);
//            cardLayout.addView(view);

        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            for (int i = 0; i < models.size(); i++) {
                final TodoModel todoModel = models.get(i);
                if (todoModel == null) continue;
                final View view = inflater.inflate(R.layout.item_todo, null, false);
                final TextView titleText = view.findViewById(R.id.item_todo_title);
                TextView timeText = view.findViewById(R.id.item_todo_time);
                TextView deleteText = view.findViewById(R.id.item_todo_delete);
                final CheckBox checkBox = view.findViewById(R.id.item_to_checkbox);
                titleText.setText(todoModel.getTitle());
                deleteTextList.add(deleteText);
                timeTextList.add(timeText);
                timeText.setText(sdf.format(todoModel.getTimestamp()));

                if (todoModel.isFinish()) {
                    checkBox.setChecked(true);
                    titleText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    titleText.setTypeface(titleText.getTypeface(), Typeface.ITALIC);
                    titleText.setText(todoModel.getTitle());
                }
                titleText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!checkBox.isChecked()) {
                            todoModel.setFinish(true);
                            todoModel.save();
                            checkBox.setChecked(true);
                            titleText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            titleText.setTypeface(titleText.getTypeface(), Typeface.ITALIC);
                            titleText.setText(todoModel.getTitle());
                        } else {
                            checkBox.setChecked(false);
                            todoModel.setFinish(false);
                            todoModel.save();
                            titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            titleText.setTypeface(normalTypeFace, Typeface.NORMAL);
                            titleText.setText(todoModel.getTitle());
                        }
                    }
                });
                deleteText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        todoModel.delete();
                        view.setVisibility(View.GONE);
                        findTodoData();
                        ToastTools.show(getContext(), "删除成功");
                    }
                });
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            todoModel.setFinish(true);
                            todoModel.save();
                            titleText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            titleText.setTypeface(titleText.getTypeface(), Typeface.ITALIC);
                            titleText.setText(todoModel.getTitle());
                        } else {
                            todoModel.setFinish(false);
                            todoModel.save();
                            titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            titleText.setTypeface(normalTypeFace, Typeface.NORMAL);
                            titleText.setText(todoModel.getTitle());
                        }
                    }
                });
                todoCardLayout.addView(view);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateBindDataEvent(UpdateBindDataEvent event) {
        findData2();
    }

    public void findData2() {
        int id2 = ShareTools.getInt(getActivity(), ShareConstants.INT_SCHEDULE_NAME_ID2, 0);
        int guanlian = ShareTools.getInt(getActivity(), ShareConstants.INT_GUANLIAN, 1);

        if (guanlian == 1) {
            isBindLayout.setVisibility(View.VISIBLE);
            bindContainer.setVisibility(View.VISIBLE);
        } else {
            isBindLayout.setVisibility(View.GONE);
            bindContainer.setVisibility(View.GONE);
            return;
        }
        if (id2 == 0) {
            bindContainer.setVisibility(View.GONE);
            return;
        } else {
            bindContainer.setVisibility(View.VISIBLE);
        }
        final ScheduleName newName = DataSupport.find(ScheduleName.class, id2);
        if (newName == null) {
            isBindLayout.setVisibility(View.VISIBLE);
            return;
        }
        isBindLayout.setVisibility(View.GONE);

        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> models = (List<TimetableModel>) t;
                if (models != null) {
                    List<Schedule> allModels = ScheduleSupport.transform(models);
                    if (allModels != null && allModels.size() != 0) {
                        curWeek = TimetableTools.getCurWeek(getActivity());
                        dayOfWeek = getDayOfWeek();
                        List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
                        list = ScheduleSupport.getColorReflect(list);
                        if (list == null) list = new ArrayList<>();
                        createCardView2(list, newName);
                    } else createCardView2(null, newName);
                }
            }
        });
    }

    public int getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek - 2;
        if (dayOfWeek == -1) dayOfWeek = 6;
        return dayOfWeek;
    }

    /**
     * 扫码、从超表导入
     */
    @OnClick(R.id.id_func_scan)
    public void toScanActivity() {
        shouldcheckPermission();
        ActivityTools.toActivityWithout(getActivity(), ScanActivity.class);
    }

    @OnClick(R.id.id_func_theme)
    public void onThemeClicked() {
        toSimportActivity();
    }

    public void showImportDialog() {
        String[] items = {"从超表课程码导入", "从超表账户导入", "从教务系统导入","从喜鹊儿导入(青果教务)"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle("课程导入")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                ActivityTools.toActivityWithout(getActivity(), ScanActivity.class);
                                break;
                            case 1:
                                toSimportActivity();
                                break;
                            case 2:
                                toSearchSchool();
                                break;
                            case 3:
                                toQingguoActivity();
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
        ;
    }

    @OnClick(R.id.id_func_multi)
    public void toMultiActivity() {
        ActivityTools.toActivityWithout(getActivity(), MultiScheduleActivity.class);
    }

    /**
     * 青果教务
     */
    @OnClick(R.id.id_func_message)
    public void toQingguoActivity() {
        ActivityTools.toActivityWithout(getActivity(), LoginActivity.class);
    }

    @OnClick(R.id.id_func_message_count)
    public void toMessageActivity() {
        ActivityTools.toActivityWithout(getActivity(), MessageActivity.class);
    }

    public void toSimportActivity() {
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_IMPORT);
        startActivityForResult(intent, MainActivity.REQUEST_IMPORT);
    }

    @OnClick(R.id.id_func_setting)
    public void toSettingActivity() {
        ActivityTools.toActivityWithout(getActivity(), MenuActivity.class);
    }

    /**
     * 接收授权页面获取的课程信息
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_IMPORT && resultCode == AuthActivity.RESULT_STATUS) {
            SuperResult result = SuperUtils.getResult(data);
            if (result == null) {
                Toasty.error(getActivity(), "result is null").show();
            } else {
                if (result.isSuccess()) {
                    List<SuperLesson> lessons = result.getLessons();
                    ScheduleName newName = ScheduleDao.saveSuperShareLessons(lessons);
                    if (newName != null) {
                        ImportTools.showDialogOnApply(getContext(), newName);
                    } else {
                        Toasty.error(getActivity(), "ScheduleName is null").show();
                    }
                } else {
                    Toasty.error(getActivity(), "" + result.getErrMsg()).show();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReloadStationEvent(ReloadStationEvent event) {
        if (event != null && event.getStationModel() != null) {
            StationManager.openStationWithout(getActivity(), event.getStationModel());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateScheduleEvent(UpdateScheduleEvent event) {
        findData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateStationHomeEvent(UpdateStationHomeEvent event) {
        findStationLocal();
    }

    /**
     * 获取添加到首页的服务站
     */
    public void findStationLocal() {
        FindMultiExecutor findMultiExecutor = DataSupport.findAllAsync(StationModel.class);
        findMultiExecutor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<StationModel> stationModels = (List<StationModel>) t;
                FuncFragment.this.stationModels.clear();
                if (stationModels == null || stationModels.size() == 0) {
                    stationGridView.setVisibility(View.GONE);
                } else {
                    stationGridView.setVisibility(View.VISIBLE);
                    FuncFragment.this.stationModels.addAll(stationModels);
                }
                FuncFragment.this.stationAdapter.notifyDataSetChanged();
            }
        });
    }


    public synchronized Set<String> getReadSet() {
        if (messagePreferences == null) return new HashSet<>();
        Set<String> r = messagePreferences.getStringSet("app_message_set", new HashSet<String>());
        Set<String> newSet = new HashSet<>(r);
        return newSet;
    }

    public void getUnreadMessageCount() {
        String deviceId = DeviceTools.getDeviceId(getContext());
        if (deviceId == null) return;
        String schoolName = ShareTools.getString(getContext(), ShareConstants.STRING_SCHOOL_NAME, "unknow");
        TimetableRequest.getMessages(getContext(), deviceId, schoolName, "only_unread_count", new Callback<ListResult<MessageModel>>() {
            @Override
            public void onResponse(Call<ListResult<MessageModel>> call, Response<ListResult<MessageModel>> response) {
                if (response == null || getContext() == null) return;
                ListResult<MessageModel> result = response.body();
                if (result == null) {
                    ToastTools.show(getActivity(), "服务器开小差了!");
                    return;
                }
                if (result.getCode() == 200) {
                    List<MessageModel> models = result.getData();
                    if (models != null) {
                        int size = 0;
                        Set<String> readSet = getReadSet();
                        for (MessageModel model : models) {
                            if (!readSet.contains(String.valueOf(model.getUnreadId()))) {
                                size++;
                            }
                        }
                        if (size > 0) {
                            messageCountView.setVisibility(View.VISIBLE);
                            messageCountView.setText("你有"+String.valueOf(size)+"条消息未读");
                        } else hideMessageCountView();
                    } else hideMessageCountView();
                } else {
                    hideMessageCountView();
                    Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<MessageModel>> call, Throwable t) {
                hideMessageCountView();
            }
        });
    }

    public void hideMessageCountView() {
        messageCountView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.id_bind_course_btn)
    public void onBindLayoutClicked() {

        FindMultiExecutor executor = DataSupport.order("time desc").findAsync(ScheduleName.class);
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(final List<T> t) {
                final List<ScheduleName> models = (List<ScheduleName>) t;
                if (t == null || models == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] items = new String[t.size()];
                        for (int i = 0; i < models.size(); i++) {
                            items[i] = models.get(i).getName();
                        }
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                                .setTitle("选择一个课表以绑定")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ShareTools.putInt(getActivity(), ShareConstants.INT_SCHEDULE_NAME_ID2, models.get(i).getId());
                                        findData2();
                                        BroadcastUtils.refreshAppWidget(getContext());
                                        Toasty.success(getActivity(), "关联成功!").show();
                                    }
                                })
                                .setCancelable(false)
                                .setNegativeButton("取消", null);
                        builder.create().show();
                    }
                });
            }
        });
    }

    @OnClick(R.id.id_func_setting_img2)
    public void onSettingLayout2Clicked() {
        String[] items = {
                "隐藏本卡片"
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("卡片设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                isBindLayout.setVisibility(View.GONE);
                                bindContainer.setVisibility(View.GONE);
                                ShareTools.putInt(getActivity(), ShareConstants.INT_GUANLIAN, 0);
                                ToastTools.show(getActivity(), "已隐藏关联课表卡片，可在工具箱中打开");
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_func_setting_img)
    public void onSettingLayoutClicked() {
        String[] items = {
                "查看关联信息",
                "重新关联课表",
                "隐藏本卡片"
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("卡片设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                int id2 = ShareTools.getInt(getActivity(), ShareConstants.INT_SCHEDULE_NAME_ID2, 0);
                                final ScheduleName newName = DataSupport.find(ScheduleName.class, id2);
                                if (newName != null) {
                                    ToastTools.show(getActivity(), "课表名称:" + newName.getName());
                                }
                                break;
                            case 1:
                                onBindLayoutClicked();
                                break;
                            case 2:
                                isBindLayout.setVisibility(View.GONE);
                                bindContainer.setVisibility(View.GONE);
                                ShareTools.putInt(getActivity(), ShareConstants.INT_GUANLIAN, 0);
                                ToastTools.show(getActivity(), "已隐藏关联课表卡片，可在工具箱中打开");
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_func_setting_img4)
    public void onHelpLayoutClicked() {
        String[] items = {
                "隐藏本卡片"
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("卡片设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                helpLayout.setVisibility(View.GONE);
                                ShareTools.putInt(getContext(), "showHelpLayout", 0);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_vip_btn)
    public void onVipBtnClicked() {
        ActivityTools.toActivityWithout(getActivity(), VipActivity.class);
    }

    @OnClick(R.id.id_func_setting_img3)
    public void onHideVipBtnClicked() {
        String[] items = {
                "隐藏本卡片"
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("卡片设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                vipLayout.setVisibility(View.GONE);
                                ShareTools.putInt(getActivity(), ShareConstants.INT_VIP_LAYOUT, 0);
                                ToastTools.show(getActivity(), "已隐藏卡片");
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_todo_setting_img)
    public void onTodoSettingImgClicked() {
        String[] items = {
                "隐藏本卡片"
        };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("卡片设置")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                todoContainerLayout.setVisibility(View.GONE);
                                ShareTools.putInt(getActivity(), ShareConstants.INT_TODO_LAYOUT, 0);
                                ToastTools.show(getActivity(), "已隐藏Todo卡片，可在工具箱中打开");
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_addtodo)
    public void addTodo() {
        ActivityTools.toActivityWithout(getActivity(), AddTodoActivity.class);
    }

    @OnClick(R.id.id_edittodo)
    public void addEditTodo() {
        if (timeTextList == null || deleteTextList == null) return;
        if (isEdit) {
            editTodoText.setText("编辑");
            editTodoText.setTextColor(Color.BLACK);
            isEdit = false;
            for (TextView delete : deleteTextList) {
                if (delete != null) {
                    delete.setVisibility(View.GONE);
                }
            }
            for (TextView time : timeTextList) {
                if (time != null) {
                    time.setVisibility(View.VISIBLE);
                }
            }
        } else {
            isEdit = true;
            editTodoText.setText("点击这里恢复");
            editTodoText.setTextColor(Color.RED);
            for (TextView delete : deleteTextList) {
                if (delete != null) {
                    delete.setVisibility(View.VISIBLE);
                }
            }
            for (TextView time : timeTextList) {
                if (time != null) {
                    time.setVisibility(View.GONE);
                }
            }
        }
    }

    private void shouldcheckPermission() {
        PermissionGen.with(getActivity())
                .addRequestCode(SUCCESSCODE)
                .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.VIBRATE
                )
                .request();
    }

    //申请权限结果的返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //权限申请成功
    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        ActivityTools.toActivityWithout(getActivity(), ScanActivity.class);
    }

    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        ToastTools.show(getContext(), "权限不足");
    }
}
