package com.zhuangfei.hputimetable.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.adapter.UploadHtmlActivity;
import com.zhuangfei.hputimetable.activity.debug.AdapterDebugTipActivity;
import com.zhuangfei.hputimetable.activity.hpu.ImportMajorActivity;
import com.zhuangfei.hputimetable.adapter.OnGryphonConfigHandler;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.SchoolPersonModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.ConfigChangeEvent;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.UpdateTools;
import com.zhuangfei.hputimetable.tools.WidgetConfig;
import com.zhuangfei.timetable.model.ScheduleConfig;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    Activity context;

    LinearLayout backLayout;

    public static final int REQUEST_IMPORT = 1;

    @BindView(R.id.id_switch_hidenotcur)
    SwitchCompat hideNotCurSwitch;

    @BindView(R.id.id_switch_hideweekends)
    SwitchCompat hideWeekendsSwitch;

    @BindView(R.id.id_checkauto)
    SwitchCompat checkedAutoSwitch;

    @BindView(R.id.id_widget_max15)
    SwitchCompat max15Switch;

    @BindView(R.id.id_widget_hideweeks)
    SwitchCompat hideWeeksSwitch;

    @BindView(R.id.id_widget_hidedate)
    SwitchCompat hideDateSwitch;

    boolean changeStatus=false;

    @BindView(R.id.id_device_text)
    TextView deviceText;

    @BindView(R.id.id_school_text)
    TextView schoolText;

    @BindView(R.id.id_school_count_text)
    TextView personCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        context = this;

        String deviceId= DeviceTools.getDeviceId(this);
        if(deviceId!=null){
            if(deviceId.length()>=8){
                deviceText.setText("UID:"+deviceId.substring(deviceId.length()-8));
            }else {
                deviceText.setText("UID:"+deviceId);
            }
        }else{
            deviceText.setText("设备号获取失败");
        }

        backLayout = findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        int hide = ShareTools.getInt(this, "hidenotcur", 0);
        if (hide == 0) {
            hideNotCurSwitch.setChecked(false);
        } else {
            hideNotCurSwitch.setChecked(true);
        }

        int alpha = ShareTools.getInt(this, "hideweekends", 0);
        if (alpha == 0) {
            hideWeekendsSwitch.setChecked(false);
        } else {
            hideWeekendsSwitch.setChecked(true);
        }

        int isIgnoreUpdate = ShareTools.getInt(this, "isIgnoreUpdate", 0);
        if (isIgnoreUpdate == 0) {
            checkedAutoSwitch.setChecked(true);
        } else {
            checkedAutoSwitch.setChecked(false);
        }

        boolean maxItem= WidgetConfig.get(this,WidgetConfig.CONFIG_MAX_ITEM);
        max15Switch.setChecked(maxItem);

        boolean hideWeeks= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_WEEKS);
        hideWeeksSwitch.setChecked(hideWeeks);

        boolean hideDate= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_DATE);
        hideDateSwitch.setChecked(hideDate);

        String schoolName=ShareTools.getString(MenuActivity.this,ShareConstants.STRING_SCHOOL_NAME,null);
        if(schoolName==null){
            schoolText.setText("未关联学校");
        }else {
            schoolText.setText(schoolName);
            getSchoolPersonCount(schoolName);
        }
    }

    public Activity getContext() {
        return context;
    }

    public void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空数据")
                .setMessage("确认后将删除本地保存的所有课程数据且无法恢复！请谨慎操作")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.clear(getContext());
                        DataSupport.deleteAll(TimetableModel.class);
                        Intent intent = new Intent(getContext(), ImportMajorActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                        getContext().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);//动画
                        getContext().finish();
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    public void getSchoolPersonCount(final String school){
        TimetableRequest.getSchoolPersonCount(this, school, new Callback<ObjResult<SchoolPersonModel>>() {
            @Override
            public void onResponse(Call<ObjResult<SchoolPersonModel>> call, Response<ObjResult<SchoolPersonModel>> response) {
                if(response==null) return;
                ObjResult<SchoolPersonModel> result=response.body();
                if(result.getCode()==200){
                    SchoolPersonModel schoolPersonModel=result.getData();
                    if(schoolPersonModel!=null){
                        personCountText.setText(schoolPersonModel.getCount()+"名校友");
                    }
                }else {
                    Toast.makeText(MenuActivity.this,result.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<SchoolPersonModel>> call, Throwable t) {
                Toast.makeText(MenuActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.id_menu_about)
    public void about() {
        ActivityTools.toActivity(MenuActivity.this, AboutActivity.class);
        finish();
    }

    @OnClick(R.id.id_menu_update2)
    public void issues() {
        try {
            UpdateTools.checkUpdate(this,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.id_menu_update)
    public void onUpdateLayoutClick() {
        Beta.checkUpgrade();
    }

    @OnClick(R.id.id_menu_share)
    public void onShareLayoutClick() {
        String content = "怪兽课表是一款免费、开源的通用型课表软件，可以无缝从超级课程表的账户以及课程码中导入数据，并且有桌面小部件和学校专区，快来体验吧，感觉好用就分享给你的朋友吧~\n下载地址：https://www.coolapk.com/apk/com.zhuangfei.hputimetable";
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("text/plain");//设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_SUBJECT, "分享怪兽课表");
        share_intent.putExtra(Intent.EXTRA_TEXT, content);//添加分享内容
        share_intent = Intent.createChooser(share_intent, "分享怪兽课表");
        startActivity(share_intent);
    }


    @OnCheckedChanged(R.id.id_switch_hidenotcur)
    public void onHideNotCurSwitchClicked(boolean b) {
        changeStatus=true;
        if (b) {
            ShareTools.putInt(this, "hidenotcur", 1);
        } else {
            ShareTools.putInt(this, "hidenotcur", 0);
        }
    }

    @OnCheckedChanged(R.id.id_switch_hideweekends)
    public void onHideWeekendsSwitchClicked(boolean b) {
        changeStatus=true;
        if (b) {
            ShareTools.putInt(this, "hideweekends", 1);
        } else {
            ShareTools.putInt(this, "hideweekends", 0);
        }
    }

    @OnCheckedChanged(R.id.id_checkauto)
    public void onCheckedAutoSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "isIgnoreUpdate", 0);
        } else {
            ShareTools.putInt(this, "isIgnoreUpdate", 1);
        }
    }

    @OnCheckedChanged(R.id.id_widget_hideweeks)
    public void onCheckedHideWeeksSwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_WEEKS,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_max15)
    public void onCheckedMax15SwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_MAX_ITEM,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_hidedate)
    public void onCheckedHideDateSwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_DATE,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnClick(R.id.id_debug)
    public void toDebug(){
        ActivityTools.toActivity(this,AdapterDebugTipActivity.class);
    }

    @OnClick(R.id.id_other)
    public void jumpTo(){
        Intent intent=new Intent(this, UploadHtmlActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(changeStatus){
            EventBus.getDefault().post(new ConfigChangeEvent());
        }
    }

    @OnClick(R.id.id_menu_modify_school)
    public void onModifyButtonClicked(){
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("修改学校")
                .setMessage("你可以修改本设备关联的学校，学校信息将作为筛选服务的重要依据")
                .setPositiveButton("修改学校", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openBindSchoolActivity();
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("取消",null);
        builder.create().show();
    }

    public void openBindSchoolActivity() {
        Intent intent = new Intent(this, BindSchoolActivity.class);
        intent.putExtra(BindSchoolActivity.FINISH_WHEN_NON_NULL,0);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_station_open_activity, R.anim.anim_station_static);//动画
        finish();
    }
}
