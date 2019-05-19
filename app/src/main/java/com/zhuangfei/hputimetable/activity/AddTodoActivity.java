package com.zhuangfei.hputimetable.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.payelves.sdk.EPay;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.listener.QueryOrderListener;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.TodoModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.event.UpdateTodoEvent;
import com.zhuangfei.hputimetable.listener.VipVerifyResult;
import com.zhuangfei.hputimetable.model.ActiveCode;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTodoActivity extends AppCompatActivity {

    @BindView(R.id.id_todo_title)
    EditText titleEdit;

    @BindView(R.id.id_set_time_display)
    TextView displayText;

    Context context;

    Date chooseDate=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        ButterKnife.bind(this);
        context=this;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }

    @OnClick(R.id.id_todo_add)
    public void onAddTodoButtonClicked(){
        String todoTitle= titleEdit.getText().toString();
        if(TextUtils.isEmpty(todoTitle)){
            ToastTools.show(this,"标题不可以为空");
            return;
        }
        if(chooseDate==null){
            ToastTools.show(this,"请选择任务结束时间");
            return;
        }

        TodoModel todoModel=new TodoModel();
        todoModel.setTitle(todoTitle);
        todoModel.setTimestamp(chooseDate.getTime());
        todoModel.setFinish(false);
        todoModel.save();
        ToastTools.show(this,"保存成功");
        EventBus.getDefault().post(new UpdateTodoEvent());
        finish();
    }

    @OnClick(R.id.id_set_time_layout)
    public void onSetTimeLayoutClicked(){
        int year=2019,month=5,day=18;
        Calendar calendar=Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH);
        day=calendar.get(Calendar.DATE);
        final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-M-d 00:00:00");
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(context,  new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;//月份加一
                try {
                    chooseDate=sdf.parse(year+"-"+month+"-"+dayOfMonth+" 00:00:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                displayText.setText(year+"年"+month+"月"+dayOfMonth+"日");
            }

        }, year, month, day);

        mDatePickerDialog.setOnCancelListener(null);
        mDatePickerDialog.show();
    }
}
