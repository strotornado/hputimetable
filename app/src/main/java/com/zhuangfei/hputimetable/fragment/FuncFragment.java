package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.activity.MessageActivity;
import com.zhuangfei.hputimetable.activity.schedule.MultiScheduleActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.ScanActivity;
import com.zhuangfei.hputimetable.activity.schedule.TimetableDetailActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleColorPool;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Administrator 刘壮飞
 */
@SuppressLint({"NewApi", "ValidFragment"})
public class FuncFragment extends LazyLoadFragment implements OnNoticeUpdateListener {

    private View mView;

    @BindView(R.id.id_cardview_layout)
    LinearLayout cardLayout;

    @BindView(R.id.id_cardview_today)
    TextView todayInfo;

    OnSwitchPagerListener onSwitchPagerListener;
    OnUpdateCourseListener onUpdateCourseListener;

    @BindView(R.id.id_func_schedulename)
    TextView scheduleNameText;

    @BindView(R.id.tv_count)
    TextView countTextView;

    @BindView(R.id.tv_counttip)
    TextView countTipTextView;

    @BindView(R.id.cv_dayview)
    CardView dayView;

    @BindView(R.id.id_mode_qinglv)
    TextView qinglvTextView;

    @BindView(R.id.id_top_nav)
    LinearLayout topNavLayout;

    @BindView(R.id.id_func_message_count)
    TextView messageCountView;

    boolean qinglvMode=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_func, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        inits();
    }

    private void inits() {
//        createDayViewBottom();
        findData();
        getUnreadMessageCount();
    }

    public void createCardView(List<Schedule> models, ScheduleName newName) {
        ScheduleColorPool colorPool=new ScheduleColorPool(getContext());
        cardLayout.removeAllViews();
        SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE");
        int curWeek = TimetableTools.getCurWeek(getActivity());
        todayInfo.setText("第" + curWeek + "周  " + sdf2.format(new Date()));

        if (newName != null) {
            scheduleNameText.setText(newName.getName());
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        if (models == null) {
            Toast.makeText(getContext(),"models is null",Toast.LENGTH_SHORT).show();
            countTextView.setText("0");
            countTipTextView.setText("你还没有添加数据呀!");
            dayView.setVisibility(View.GONE);

            View view = inflater.inflate(R.layout.item_empty, null, false);
            TextView infoText = view.findViewById(R.id.item_empty);
            view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onSwitchPagerListener != null) {
                        onSwitchPagerListener.onPagerSwitch();
                    }
                }
            });
            infoText.setText("本地没有数据,去添加!");
            cardLayout.addView(view);
        } else if (models.size() == 0) {
            Toast.makeText(getContext(),"models size= 0",Toast.LENGTH_SHORT).show();
			View view=inflater.inflate(R.layout.item_empty,null ,false);
			TextView infoText=view.findViewById(R.id.item_empty);
			view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(onSwitchPagerListener!=null){
						onSwitchPagerListener.onPagerSwitch();
					}
				}
			});
			cardLayout.addView(view);

        } else {
            countTextView.setText("" + models.size());
            if (models.size() < 3) countTipTextView.setText("今天课程较少，非常轻松~");
            else if (models.size() < 5) countTipTextView.setText("今天课程适中，加油~");
            else countTipTextView.setText("好多的课呀，有点慌~");
            dayView.setVisibility(View.VISIBLE);

            for (int i = 0; i < models.size(); i++) {
                final Schedule schedule = models.get(i);
                if (schedule == null) continue;
                View view = inflater.inflate(R.layout.item_cardview, null, false);
                TextView startText = view.findViewById(R.id.id_item_start);
                TextView nameText = view.findViewById(R.id.id_item_name);
                TextView roomText = view.findViewById(R.id.id_item_room);
                View colorView = view.findViewById(R.id.id_item_color);
                colorView.setBackgroundColor(colorPool.getColorAuto(schedule.getColorRandom()));

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(colorPool.getColorAuto(schedule.getColorRandom()));
                gd.setCornerRadius(ScreenUtils.dip2px(getContext(),3));
                startText.setBackgroundDrawable(gd);

                String name = schedule.getName();
                String room = schedule.getRoom();
                if (TextUtils.isEmpty(name)) name = "课程名未知";
                if (TextUtils.isEmpty(room)) room = "上课地点未知";
                nameText.setText(name);
                roomText.setText(room);
                startText.setText(schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1)+"节");
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
                    if (allModels != null) {
                        int curWeek = TimetableTools.getCurWeek(getActivity());
                        Calendar c = Calendar.getInstance();
                        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                        dayOfWeek = dayOfWeek - 2;
                        if (dayOfWeek == -1) dayOfWeek = 6;
                        List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
                        list=ScheduleSupport.getColorReflect(list);
                        if(list==null) list=new ArrayList<>();
                        createCardView(list, newName);
                    } else createCardView(null, newName);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSwitchPagerListener) {
            onSwitchPagerListener = (OnSwitchPagerListener) context;
        }
        if (context instanceof OnUpdateCourseListener) {
            onUpdateCourseListener = (OnUpdateCourseListener) context;
        }
    }

    /**
     * 扫码、从超表导入
     */
    @OnClick(R.id.id_func_scan)
    public void toScanActivity() {
        String[] items={"从课程码导入","从超表账户导入"};
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle("从超级课程表导入")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                ActivityTools.toActivityWithout(getActivity(), ScanActivity.class);
                                break;
                            case 1:
                                toSimportActivity();
                                break;
                        }
                    }
                })
                .setNegativeButton("取消",null);
        builder.create().show();;
    }

    @OnClick(R.id.id_func_multi)
    public void toMultiActivity() {
        ActivityTools.toActivityWithout(getActivity(), MultiScheduleActivity.class);
    }

    @OnClick(R.id.id_func_message)
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
                        showDialogOnApply(newName);
                    } else {
                        Toasty.error(getActivity(), "ScheduleName is null").show();
                    }
                } else {
                    Toasty.error(getActivity(), "" + result.getErrMsg()).show();
                }
            }
        }
    }

    private void showDialogOnApply(final ScheduleName name) {
        if (name == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("你导入的数据已存储在多课表[" + name.getName() + "]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ScheduleDao.changeFuncStatus(getActivity(), true);
                        ScheduleDao.applySchedule(getActivity(), name.getId());
                        BroadcastUtils.refreshAppWidget(getActivity());
                        if (onSwitchPagerListener != null) {
                            onSwitchPagerListener.onPagerSwitch();
                        }
                        if (onUpdateCourseListener != null) {
                            onUpdateCourseListener.onUpdateData();
                        }
                        findData();
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("稍后设置", null);
        builder.create().show();
    }

    @Override
    public void onUpdateNotice() {
        if (ScheduleDao.isNeedFuncUpdate(getActivity())) {
            if (todayInfo.getText() != null) {
                findData();
            }
            ScheduleDao.changeFuncStatus(getActivity(), false);
        }
    }

    @OnClick(R.id.id_week_view)
    public void toScheduleFragment() {
        if (onSwitchPagerListener != null) {
            onSwitchPagerListener.onPagerSwitch();
        }
    }

    @OnClick(R.id.id_mode_qinglv)
    public void changeQinglvMode(){
        if(!qinglvMode){
            qinglvTextView.setBackground(getResources().getDrawable(R.drawable.border_qinglv_active));
            qinglvMode=true;
            topNavLayout.setVisibility(View.GONE);
        }else{
            qinglvTextView.setBackground(getResources().getDrawable(R.drawable.border_qinglv));
            qinglvMode=false;
            topNavLayout.setVisibility(View.VISIBLE);
        }
    }


    public void getUnreadMessageCount(){
        String deviceId= DeviceTools.getDeviceId(getContext());
        if(deviceId==null) return;
        String school="unknow";
        TimetableRequest.getMessages(getContext(), deviceId,school,"only_unread_count", new Callback<ListResult<MessageModel>>() {
            @Override
            public void onResponse(Call<ListResult<MessageModel>> call, Response<ListResult<MessageModel>> response) {
                if(response==null) return;
                ListResult<MessageModel> result=response.body();
                if(result.getCode()==200){
                    List<MessageModel> models=result.getData();
                    if(models!=null){
                        int size=models.size();
                        if(size>0){
                            messageCountView.setVisibility(View.VISIBLE);
                            messageCountView.setText(String.valueOf(size));
                        }else hideMessageCountView();
                    }else hideMessageCountView();
                }else {
                    hideMessageCountView();
                    Toast.makeText(getContext(),result.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<MessageModel>> call, Throwable t) {
                hideMessageCountView();
            }
        });
    }

    public void hideMessageCountView(){
        messageCountView.setVisibility(View.GONE);
    }
}
