package com.zhuangfei.hputimetable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class CreateScheduleNameActivity extends AppCompatActivity {

    @BindView(R.id.et_schedulename)
    EditText nameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_schedule_name);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.cv_save)
    public void save() {
        String name=nameEdit.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toasty.warning(this,"课表名称不可为空", Toast.LENGTH_SHORT).show();
        }else if (name.equals("默认课表")) {
            Toasty.error(this, "名称不合法").show();
        }else{
            ScheduleName scheduleName=new ScheduleName();
            scheduleName.setName(name);
            scheduleName.setTime(System.currentTimeMillis());
            boolean isSave=scheduleName.save();
            if(isSave){
                Toasty.success(this,"创建课表成功", Toast.LENGTH_SHORT).show();
                goBack();
            }else{
                Toasty.error(this,"创建课表失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @OnClick(R.id.id_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this,MultiScheduleActivity.class);
    }
}
