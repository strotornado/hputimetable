package com.zhuangfei.hputimetable.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BindSchoolActivity extends AppCompatActivity {

    @BindView(R.id.id_school_edit)
    EditText schoolEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_school);
        ButterKnife.bind(this);

        String schoolName=ShareTools.getString(BindSchoolActivity.this,ShareConstants.STRING_SCHOOL_NAME,null);
        if(!TextUtils.isEmpty(schoolName)){
            finish();
        }
    }

    @OnClick(R.id.id_bind_button)
    public void onBindButtonClicked(){
        String school=schoolEdit.getText().toString();
        if(!TextUtils.isEmpty(school)&&!TextUtils.isEmpty(school.trim())){
            bindSchoolForWeb(school.trim());
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.anim_station_static,R.anim.anim_station_close_activity);
    }

    public void bindSchoolForWeb(final String school){
        String deviceId= DeviceTools.getDeviceId(this);
        if(deviceId==null) return;
        TimetableRequest.bindSchool(this, deviceId, school, new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                if(response==null) return;
                BaseResult result=response.body();
                if(result.getCode()==200){
                    ShareTools.putString(BindSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME,school);
                    Toast.makeText(BindSchoolActivity.this,"关联成功",Toast.LENGTH_SHORT).show();
                    ActivityTools.toActivity(BindSchoolActivity.this,MainActivity.class);
                }else {
                    Toast.makeText(BindSchoolActivity.this,result.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toast.makeText(BindSchoolActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
