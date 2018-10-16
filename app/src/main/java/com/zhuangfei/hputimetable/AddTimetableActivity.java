package com.zhuangfei.hputimetable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.AddModel;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class AddTimetableActivity extends Activity {

    private static final String TAG = "AddTimetableActivity";
    @BindView(R.id.et_name)
    public EditText nameEditText;

    @BindView(R.id.et_teacher)
    public EditText teacherEditText;

    @BindView(R.id.id_add_container)
    public LinearLayout containerLayout;

    @BindView(R.id.cv_add)
    public CardView addDuringCardView;

    private Class returnClass = MainActivity.class;
    private LayoutInflater inflate;

    public static final String KEY_NAME = "name";
    public static final String KEY_ROOM = "room";
    public static final String KEY_TEACHER = "teacher";
    public static final String KEY_START = "start";
    public static final String KEY_STEP = "step";
    public static final String KEY_DAY = "day";
    public static final String KEY_WEEKS = "weeks";

    private List<String> dayList = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
    private List<String> nodeList = new ArrayList<>();

    private List<Boolean> statusList=new ArrayList<>();

    private int curScheduleNameId=-1;
    private ScheduleName scheduleName=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable2);
        ButterKnife.bind(this);
        initsData();
    }

    private void initsData() {
        inflate = LayoutInflater.from(this);
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        String name = BundleTools.getString(this, KEY_NAME, "");
        String room = BundleTools.getString(this, KEY_ROOM, "");
        String teacher = BundleTools.getString(this, KEY_TEACHER, "");
        String weeks=BundleTools.getString(this,KEY_WEEKS,"");

        int day= (int) BundleTools.getInt(this,KEY_DAY,1);
        int start= (int) BundleTools.getInt(this,KEY_START,1);
        int step= (int) BundleTools.getInt(this,KEY_STEP,1);

        nameEditText.setText("" + name);
        teacherEditText.setText(teacher);

        for (int i = 1; i <= 15; i++) {
            nodeList.add("第 " + i + " 节");
        }

        for(int i=1;i<=25;i++){
            statusList.add(false);
        }
        statusList.set(0,true);

        curScheduleNameId=ShareTools.getInt(this,ShareConstants.INT_SCHEDULE_NAME_ID,-1);
        if(curScheduleNameId==-1){
            Toasty.error(this,"你还没有课表，请前去创建或应用",Toast.LENGTH_SHORT).show();
            goBack();
        }else{
            scheduleName=DataSupport.find(ScheduleName.class,curScheduleNameId);
            addItemView();
            View itemView = containerLayout.getChildAt(0);
            if(itemView!=null){
                TextView time = itemView.findViewById(R.id.et_time);
                TextView weeksText = itemView.findViewById(R.id.et_weeks);
                TextView roomText = itemView.findViewById(R.id.et_room);
                AddModel model= (AddModel) itemView.getTag();
                model.setDay(day);
                model.setStart(start);
                model.setEnd(start+step-1);
                model.setRoom(room);
                List<Integer> weekList=TimetableTools.getWeekList(weeks);
                List<Boolean> statusList=model.getStatus();
                for(int i=1;i<=25;i++){
                    if(weekList.contains(i)){
                        statusList.set(i-1,true);
                    }else{
                        statusList.set(i-1,false);
                    }
                }
                time.setText("周" + getDayString(model.getDay()) + "    第" + model.getStart() + " - " + model.getEnd() + "节");
                if(weekList!=null&&weekList.size()>0){
                    weeksText.setText(weekList.toString());
                }
                roomText.setText(model.getRoom());
            }
        }
    }

    @OnClick(R.id.cv_add)
    public void addItemView() {
        final View itemView = inflate.inflate(R.layout.item_add, null, false);
        ImageView iv = itemView.findViewById(R.id.iv_delete);
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (containerLayout.indexOfChild(itemView) != -1) {
                    if (containerLayout.getChildCount() <= 1) {
                        Toasty.warning(AddTimetableActivity.this, "至少保留一个时间段", Toast.LENGTH_SHORT).show();
                    } else {
                        containerLayout.removeView(itemView);
                    }
                }
            }
        });

        final AddModel model = new AddModel();
        final LinearLayout timelayout = itemView.findViewById(R.id.ll_time);
        final LinearLayout weeksLayout = itemView.findViewById(R.id.ll_weeks);
        final TextView weeks=itemView.findViewById(R.id.et_weeks);
        final TextView time = itemView.findViewById(R.id.et_time);
        timelayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeAlert(model, time);
            }
        });

        weeksLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showWeekAlert(model,weeks);
            }
        });
        itemView.setTag(model);
        containerLayout.addView(itemView);
    }

    @OnClick(R.id.tv_save)
    public void save() {
        String name = nameEditText.getText().toString().trim();
        String teacher = teacherEditText.getText().toString().trim();
        String term = ShareTools.getString(this, ShareConstants.KEY_CUR_TERM, "term");

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(teacher)) {
            Toasty.warning(this, "请填写课程以及教师名称!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TimetableModel> models = new ArrayList<>();
        for (int i = 0; i < containerLayout.getChildCount(); i++) {
            View v = containerLayout.getChildAt(i);
            AddModel model = (AddModel) v.getTag();
            EditText roomEdit = v.findViewById(R.id.et_room);
            String room = roomEdit.getText().toString();
            List<Integer> weekList=new ArrayList<>();
            for(int m=0;m<model.getStatus().size();m++){
                if(model.getStatus().get(m)){
                    weekList.add(m+1);
                }
            }
            if (TextUtils.isEmpty(room) || weekList.size() == 0) {
                Toasty.warning(this, "请补充时间段" + (i + 1) + "的信息", Toast.LENGTH_SHORT).show();
                return;
            }

            TimetableModel timetableModel = new TimetableModel(term, name, room, teacher, weekList, model.getStart(), model.getEnd() - model.getStart() + 1, model.getDay(), -1, "");
            timetableModel.setScheduleName(scheduleName);
            models.add(timetableModel);
        }
        DataSupport.saveAll(models);
        ShareTools.putInt(this, "course_update", 1);
        BroadcastUtils.refreshAppWidget(this);
        Toasty.success(this, "保存成功", Toast.LENGTH_SHORT).show();
        goBack();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.tv_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, returnClass,new BundleModel().put("item",1));
    }

    public void showTimeAlert(final AddModel model, final TextView time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        View view = LayoutInflater.from(this).inflate(R.layout.view_select_time, null, false);

        WheelPicker wpDay = view.findViewById(R.id.wp_day);
        WheelPicker wpStart = view.findViewById(R.id.wp_start);
        final WheelPicker wpEnd = view.findViewById(R.id.wp_end);

        wpDay.setData(dayList);
        wpStart.setData(nodeList);
        wpEnd.setData(nodeList);

        wpDay.setSelectedItemPosition(model.getDay() - 1);
        wpStart.setSelectedItemPosition(model.getStart() - 1);
        wpEnd.setSelectedItemPosition(model.getEnd() - 1);

        wpDay.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                model.setDay(position + 1);
            }
        });

        wpStart.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                model.setStart(position + 1);
                if (model.getEnd() < model.getStart()) {
                    wpEnd.setSelectedItemPosition(model.getStart() - 1);
                    model.setEnd(model.getStart() - 1);
                }
            }
        });

        wpEnd.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                model.setEnd(position + 1);
                if (model.getEnd() < model.getStart()) {
                    wpEnd.setSelectedItemPosition(model.getStart() - 1);
                    model.setEnd(model.getStart() - 1);
                }
            }
        });

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.getEnd() < model.getStart()) {
                    Toasty.error(AddTimetableActivity.this, "结束节次应该大于等于开始节次", Toast.LENGTH_SHORT).show();
                } else {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    time.setText("周" + getDayString(model.getDay()) + "    第" + model.getStart() + " - " + model.getEnd() + "节");
                }
            }
        });

        alertDialog.show();
    }

    public void showWeekAlert(final AddModel model, final TextView week) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        View view = LayoutInflater.from(this).inflate(R.layout.view_select_week, null, false);
        GridView gridView = view.findViewById(R.id.ll_week);
        final SelectWeekAdapter adapter=new SelectWeekAdapter(this,model.getStatus());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                boolean status=model.getStatus().get(i);
                if(status){
                    model.getStatus().set(i,false);
                }else{
                    model.getStatus().set(i,true);
                }
                adapter.notifyDataSetChanged();
            }
        });
//        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> weekList=new ArrayList<>();
                for(int m=0;m<model.getStatus().size();m++){
                    if(model.getStatus().get(m)){
                        weekList.add(m+1);
                    }
                }
                if(weekList.size()==0){
                    Toasty.error(AddTimetableActivity.this, "最少选择一周", Toast.LENGTH_SHORT).show();
                }else{
                    week.setText(weekList.toString());
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
            }
        });

        alertDialog.show();
    }


    public String getDayString(int day) {
        String str = "一";
        switch (day) {
            case 1:
                str = "一";
                break;
            case 2:
                str = "二";
                break;
            case 3:
                str = "三";
                break;
            case 4:
                str = "四";
                break;
            case 5:
                str = "五";
                break;
            case 6:
                str = "六";
                break;
            case 7:
                str = "日";
                break;
        }
        return str;
    }
}
