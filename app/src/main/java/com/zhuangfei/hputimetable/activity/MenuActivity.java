package com.zhuangfei.hputimetable.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.listener.QueryOrderListener;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.BuglyLog;
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
import com.zhuangfei.hputimetable.event.UpdateBindDataEvent;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.UpdateTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.hputimetable.tools.WidgetConfig;
import com.zhuangfei.timetable.model.ScheduleConfig;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    @BindView(R.id.id_show_qinglv)
    SwitchCompat showQinglvSwitch;

    @BindView(R.id.id_switch_firstsunday)
    SwitchCompat firstSundaySwitch;

    boolean changeStatus=false;
    boolean changeStatus2=false;

    @BindView(R.id.id_device_text)
    TextView deviceText;

    @BindView(R.id.id_school_text)
    TextView schoolText;

    @BindView(R.id.id_school_count_text)
    TextView personCountText;

    @BindView(R.id.tv_startTime)
    TextView startTimeText;

    @BindView(R.id.id_vip_btn)
    LinearLayout vipButton;

    @BindView(R.id.id_vip_expire)
    TextView expireText;

    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:MM");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        inits();
    }

    public void checkVip(){
        if(VipTools.isVip(this)){
            updateTopText();
            vipButton.setVisibility(View.GONE);
            expireText.setVisibility(View.VISIBLE);
            PayLicense license=VipTools.getLocalLicense(this);

            expireText.setText("有效期至: "+sdf.format(new Date(Long.parseLong(license.getExpire()))));
            PayTools.checkPay(this, license,new QueryOrderListener() {
                @Override
                public void onFinish(boolean isSuccess, String msg, QueryOrderModel model) {
                    if(!isSuccess){
                        if(msg!=null&&msg.indexOf("订单不存在")!=-1){
                            showDeleteLicenseDialog();
                            cancelVip();
                        }
                    }
                    ToastTools.show(MenuActivity.this,isSuccess+";"+msg);
                }
            });
        }else{
            cancelVip();
        }
    }

    private void showDeleteLicenseDialog() {
        VipTools.unregisterVip();
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("高级版被撤销")
                .setMessage("经过系统检测，您的高级版凭证非正版，证书已被删除！请支持正版，感谢您的支持，如果本检测有误，请联系客服进行申诉:1193600556@qq.com")
                .setPositiveButton("我知道了", null);
        builder.create().show();
    }

    private void cancelVip() {
        updateTopText();
        vipButton.setVisibility(View.VISIBLE);
        expireText.setVisibility(View.GONE);
    }

    public void updateTopText(){
//        String deviceId= DeviceTools.getDeviceId(this);
//        if(deviceId!=null){
//            if(deviceId.length()>=8){
//                deviceText.setText("UID:"+deviceId.substring(deviceId.length()-8));
//            }else {
//                deviceText.setText("UID:"+deviceId);
//            }
//        }else{
//            deviceText.setText("设备号获取失败");
//        }

        if(VipTools.isVip(this)){
            deviceText.setText("高级版");
        }else{
            deviceText.setText("普通版)");
        }
    }

    private void inits() {
        context = this;
        updateTopText();
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

        int show = ShareTools.getInt(this, ShareConstants.INT_GUANLIAN, 1);
        if (show == 1) {
            showQinglvSwitch.setChecked(true);
        } else {
            showQinglvSwitch.setChecked(false);
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

        int isFirstSunday = ShareTools.getInt(this, "isFirstSunday", 0);
        if (isFirstSunday == 0) {
            firstSundaySwitch.setChecked(false);
        } else {
            firstSundaySwitch.setChecked(true);
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

        String startTime=ShareTools.getString(this,ShareConstants.STRING_START_TIME,null);
        if (TextUtils.isEmpty(startTime)) {
            startTimeText.setText("未设置");
        }else{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2=new SimpleDateFormat("yyyy/MM/dd");
            try{
                Date date=sdf.parse(startTime);
                startTimeText.setText(sdf2.format(date));
            }catch (Exception e){
                BuglyLog.e("MenuActivity",e.getMessage(),e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkVip();
    }

    @OnClick(R.id.id_vip_btn)
    public void onVipBtnClicked(){
        ActivityTools.toActivityWithout(this, VipActivity.class);
    }

    @OnClick(R.id.id_set_maxcount)
    public void onSetMaxCountClicked(){
        if(!VipTools.isVip(this)){
            VipTools.showAlertDialog(this);
            return;
        }
        String[] items=new String[13];
        for(int i=0;i<=12;i++){
            items[i]=""+(i+8)+"节";
        }
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("设置节次")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.putInt(MenuActivity.this,"maxCount",(i+8));
                        ToastTools.show(MenuActivity.this,"设置成功!");
                        changeStatus=true;
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消",null);
        builder.create().show();
    }

    @OnClick(R.id.id_set_starttime_layout)
    public void onSetStartTimeLayoutClicked(){
        if(!VipTools.isVip(this)){
            VipTools.showAlertDialog(this);
            return;
        }
        final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-M-d 00:00:00");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(context,  new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;//月份加一
                try {
                    Date date=sdf.parse(year+"-"+month+"-"+dayOfMonth+" 00:00:00");
                    String time=sdf2.format(date);
                    int curweek= TimetableTools.getCurWeek(MenuActivity.this);
                    ShareTools.putString(MenuActivity.this,ShareConstants.STRING_START_TIME,time);
                    ToastTools.show(MenuActivity.this,"设置成功，当前周:"+curweek);
                    EventBus.getDefault().post(new UpdateScheduleEvent());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startTimeText.setText(year+"/"+month+"/"+dayOfMonth);
            }

        }, 2019, 4, 14);
        mDatePickerDialog.setOnCancelListener(null);
        mDatePickerDialog.show();
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
        String content = "怪兽课表是一款免费的通用型课表软件，可以无缝从超级课程表的账户以及课程码中导入数据，并且有桌面小部件和服务站功能，快来体验吧，感觉好用就分享给你的朋友吧~\n下载地址：https://www.coolapk.com/apk/com.zhuangfei.hputimetable";
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

    @OnCheckedChanged(R.id.id_show_qinglv)
    public void onShowQinglvSwitchClicked(boolean b) {
        changeStatus2=true;
        if (b) {
            ShareTools.putInt(this, ShareConstants.INT_GUANLIAN, 1);
        } else {
            ShareTools.putInt(this,  ShareConstants.INT_GUANLIAN, 0);
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

    @OnCheckedChanged(R.id.id_switch_firstsunday)
    public void onFirstSundaySwitchClicked(boolean b) {
        if(!VipTools.isVip(this)){
            VipTools.showAlertDialog(this);
            return;
        }
        if (b) {
            ShareTools.putInt(this, "isFirstSunday", 1);
        } else {
            ShareTools.putInt(this, "isFirstSunday", 0);
        }
        changeStatus=true;
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

    public void showDialog(String title,String message){
        AlertDialog alertDialog=new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("我知道了",null)
                .create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(changeStatus){
            EventBus.getDefault().post(new ConfigChangeEvent());
        }
        if(changeStatus2){
            EventBus.getDefault().post(new UpdateBindDataEvent());
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

    @OnClick(R.id.id_set_theme_layout)
    public void onSetThemeLayoutClicked(){
        if(!VipTools.isVip(this)){
            VipTools.showAlertDialog(this);
            return;
        }
        String[] items={
                "红色主题",
                "蓝色主题",
                "黑色主题"
        };
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("修改学校")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.putInt(MenuActivity.this,ShareConstants.INT_THEME,i);
                        ToastTools.show(MenuActivity.this,"设置成功，重启App生效");
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
