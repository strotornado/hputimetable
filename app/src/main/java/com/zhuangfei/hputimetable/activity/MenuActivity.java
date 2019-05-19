package com.zhuangfei.hputimetable.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payelves.sdk.EPay;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.enums.EPayResult;
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
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.SchoolPersonModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.CheckVipOrderEvent;
import com.zhuangfei.hputimetable.event.ConfigChangeEvent;
import com.zhuangfei.hputimetable.event.UpdateBindDataEvent;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.event.UpdateTodoEvent;
import com.zhuangfei.hputimetable.listener.OnExportProgressListener;
import com.zhuangfei.hputimetable.listener.VipVerifyResult;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.CalendarReminderUtils;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.FileTools;
import com.zhuangfei.hputimetable.tools.Md5Tools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.ThemeManager;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.UpdateTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.hputimetable.tools.WidgetConfig;
import com.zhuangfei.smartalert.core.LoadAlert;
import com.zhuangfei.smartalert.core.MessageAlert;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleConfig;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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

    @BindView(R.id.id_switch_weekdayfirst)
    SwitchCompat weekdayFirstSwitch;

    @BindView(R.id.id_widget_alpha1)
    SwitchCompat alpha1Switch;

    @BindView(R.id.id_widget_textcolor)
    SwitchCompat whiteTextColorSwitch;

    @BindView(R.id.id_show_todo)
    SwitchCompat showTodoSwitch;

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

    boolean effect=false;
    boolean excute=false;

    MessageAlert loadAlert;

    @BindView(R.id.id_time_set_tip)
    TextView timeSetTipText;

    @BindView(R.id.id_maxcount)
    TextView maxCountText;

    @BindView(R.id.statuslayout)
    View statusView;

    @BindView(R.id.id_back_img)
    ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ViewTools.setTransparent(this);
        ButterKnife.bind(this);
        inits();
        checkVip();
//        showOldVersionHelp();
//        showOldVersionHelp2();
    }

    public void showOldVersionHelp(){
        int old=ShareTools.getInt(this,"oldVip",1);
        if(old==1){
            showDialog("高级版证书升级","由于安全策略升级，在旧版上开通的高级版凭证将全部被弃用，所有用户都会被降级到普通用户，之前开通过高级版的用户可在工具箱-高级版证书恢复中找回");
            ShareTools.putInt(this,"oldVip",0);
        }
    }

    public void showOldVersionHelp2(){
        int old=ShareTools.getInt(this,"oldVip2",1);
        if(old==1){
            showDialog("高级版证书升级","v1.1.8版本3.3元高级版的证书过期时间我错误的设置成了一个月，应该是三个月，请在工具箱-高级版证书恢复中生成新证书，感谢您的支持和理解");
            ShareTools.putInt(this,"oldVip2",0);
        }
    }

    public void checkVip(){
        final VipVerifyResult result=VipTools.isVip(this);
        if(result!=null&&result.isSuccess()){
            updateTopText();
            vipButton.setVisibility(View.GONE);
            expireText.setVisibility(View.VISIBLE);
            final PayLicense license=result.getLicense();
            if(license==null){
                showDialog("验证失败","你可能未授予存储权限，故导致证书文件未生成，请授予权限，然后重新生成证书");
                cancelVip();
                return;
            }
            expireText.setText("有效期至: "+sdf.format(new Date(Long.parseLong(license.getExpire()))));
        }else{
            cancelVip();
        }
        String deviceId=DeviceTools.getDeviceId(context);
        if(TextUtils.isEmpty(deviceId)){
            showDialog("权限不足","无法获取设备ID，证书验证失败，请先开启读取设备IMEI权限");
            return;
        }
        if(result==null||result.getLicense()==null){
            return;
        }
        if(result!=null&&result.isNeedVerify()){
            final String finalLastModifyMd = VipTools.getLastModifyMd5(context);
            final String finalDeviceId = deviceId;
            PayTools.checkPay(this, result.getLicense(),new QueryOrderListener() {
                @Override
                public void onFinish(boolean isSuccess, String msg, QueryOrderModel model) {
                    boolean ok=VipTools.checkOrderResult(MenuActivity.this,isSuccess,msg,model,result.getLicense());
                    if(!ok){
                        VipTools.showDeleteLicenseDialog(MenuActivity.this);
                        cancelVip();
                    }else if(isSuccess){
                        if(model!=null&&model.getPayStatus()!=null&&model.getPayStatus().equals("SUCCESS")){
                            if(model!=null&&model.getOrderId()!=null&&(model.getOrderId().indexOf(finalDeviceId)!=-1||
                                    model.getOrderId().indexOf(result.getLicense().getUserId2())!=-1)){
                                ShareTools.putString(MenuActivity.this,"lastModify",""+ finalLastModifyMd);
                                updateTopText();
                                vipButton.setVisibility(View.GONE);
                                expireText.setVisibility(View.VISIBLE);
                                final PayLicense license=result.getLicense();
                                expireText.setText("有效期至: "+sdf.format(new Date(Long.parseLong(license.getExpire()))));
                                ToastTools.show(getContext(),"证书验证成功!");
                            }
                        }
                    }else{
                        ToastTools.show(getContext(),"验证失败:"+msg);
                    }
                }
            });
        }
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

        if(VipTools.isVip(this).isSuccess()){
            deviceText.setText("已开通高级版");
        }else{
            deviceText.setText("普通版");
        }
    }

    private void inits() {
        context = this;
        backImage.setColorFilter(Color.WHITE);
        updateTopText();
        try {
            int statusHeight = ViewTools.getStatusHeight(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight);
            statusView.setLayoutParams(lp);
        } catch (Exception e) {
            BuglyLog.e("FuncFragment", "onViewCreated", e);
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

        int isWeekViewFirst = ShareTools.getInt(this, "isWeekViewFirst", 0);
        if (isWeekViewFirst == 0) {
            weekdayFirstSwitch.setChecked(false);
        } else {
            weekdayFirstSwitch.setChecked(true);
        }

        int isShowTodo = ShareTools.getInt(this, ShareConstants.INT_TODO_LAYOUT, 1);
        if (isShowTodo == 0) {
            showTodoSwitch.setChecked(false);
        } else {
            showTodoSwitch.setChecked(true);
        }

        boolean maxItem= WidgetConfig.get(this,WidgetConfig.CONFIG_MAX_ITEM);
        max15Switch.setChecked(maxItem);

        boolean hideWeeks= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_WEEKS);
        hideWeeksSwitch.setChecked(hideWeeks);

        boolean hideDate= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_DATE);
        hideDateSwitch.setChecked(hideDate);

        boolean alpha1= WidgetConfig.get(this,WidgetConfig.CONFIG_ALPHA1);
        alpha1Switch.setChecked(alpha1);

        boolean textColorWhite= WidgetConfig.get(this,WidgetConfig.CONFIG_TEXT_COLOR_WHITE);
        whiteTextColorSwitch.setChecked(textColorWhite);

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
        effect=true;
        //todo
        int maxCount=ShareTools.getInt(MenuActivity.this,"maxCount",10);
        maxCountText.setText(""+maxCount);

        String time= ShareTools.getString(this,"schedule_time",null);
        if(TextUtils.isEmpty(time)){
            timeSetTipText.setText("未设置");
        }else{
            timeSetTipText.setText("已设置");
        }
    }

    //除了第一次之外都需要执行检查
    //防止和onCreate中的检查冲突
    @Override
    protected void onResume() {
        super.onResume();
        if(!excute){
            excute=true;
        }else{
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0x123);
                }
            },500);
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                checkVip();
            }catch (Exception e){
            }
        }
    };

    @OnClick(R.id.id_vip_btn)
    public void onVipBtnClicked(){
        VipVerifyResult result=VipTools.isVip(this);
        if(result.isSuccess()){
            showDialog("您已开通","已开通高级版且可用，无需再次开通!");
            checkVip();
            return;
        }
        if(result.isNeedVerify()){
            showDialog("您已开通","已开通高级版，但是存在风险，请连接网络进行验证即可!");
            checkVip();
            return;
        }
        ActivityTools.toActivityWithout(this, VipActivity.class);
    }

    @OnClick(R.id.id_set_maxcount)
    public void onSetMaxCountClicked(){
        if(!checkVipStatus()){
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
                        maxCountText.setText(""+(i+8));
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
        final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-M-d 00:00:00");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String localStartTime=ShareTools.getString(MenuActivity.this,ShareConstants.STRING_START_TIME,null);
        int year=2019,month=5,day=18;
        if(localStartTime==null){
            Calendar calendar=Calendar.getInstance();
            year=calendar.get(Calendar.YEAR);
            month=calendar.get(Calendar.MONTH);
            day=calendar.get(Calendar.DATE);
        }else{
            try {
                Date date=sdf.parse(localStartTime);
                Calendar calendar=Calendar.getInstance();
                calendar.setTime(date);
                year=calendar.get(Calendar.YEAR);
                month=calendar.get(Calendar.MONTH);
                day=calendar.get(Calendar.DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(context,  new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;//月份加一
                try {
                    Date date=sdf.parse(year+"-"+month+"-"+dayOfMonth+" 00:00:00");
                    String time=sdf2.format(date);
                    ShareTools.putString(MenuActivity.this,ShareConstants.STRING_START_TIME,time);
                    int curweek= TimetableTools.getCurWeek(MenuActivity.this);
                    ToastTools.show(MenuActivity.this,"设置成功，当前周:"+curweek);
                    EventBus.getDefault().post(new UpdateScheduleEvent());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startTimeText.setText(year+"/"+month+"/"+dayOfMonth);
            }

        }, year, month, day);
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
                        personCountText.setText(schoolPersonModel.getCount()+"校友");
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
        setChangeStatus(true);
        if (b) {
            ShareTools.putInt(this, "hidenotcur", 1);
        } else {
            ShareTools.putInt(this, "hidenotcur", 0);
        }
    }

    @OnCheckedChanged(R.id.id_switch_hideweekends)
    public void onHideWeekendsSwitchClicked(boolean b) {
        setChangeStatus(true);
        if (b) {
            ShareTools.putInt(this, "hideweekends", 1);
        } else {
            ShareTools.putInt(this, "hideweekends", 0);
        }
    }

    @OnCheckedChanged(R.id.id_show_qinglv)
    public void onShowQinglvSwitchClicked(boolean b) {
        setChangeStatus2(true);
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
        if(effect&&!checkVipStatus()){
            return;
        }
        if (b) {
            ShareTools.putInt(this, "isFirstSunday", 1);
        } else {
            ShareTools.putInt(this, "isFirstSunday", 0);
        }
        setChangeStatus(true);
    }

    /**
     * 周视图在第一屏
     * @param b
     */
    @OnCheckedChanged(R.id.id_switch_weekdayfirst)
    public void onWeekDayFirstSwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        if (b) {
            ShareTools.putInt(this, "isWeekViewFirst", 1);
        } else {
            ShareTools.putInt(this, "isWeekViewFirst", 0);
        }
        if(effect){
            ToastTools.show(MenuActivity.this,"设置成功，重启App生效");
        }
        setChangeStatus(true);
    }

    @OnCheckedChanged(R.id.id_show_todo)
    public void onShowTodoSwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        if (b) {
            ShareTools.putInt(this, ShareConstants.INT_TODO_LAYOUT, 1);
        } else {
            ShareTools.putInt(this, ShareConstants.INT_TODO_LAYOUT, 0);
        }
        EventBus.getDefault().post(new UpdateTodoEvent());
    }

    @OnCheckedChanged(R.id.id_widget_hideweeks)
    public void onCheckedHideWeeksSwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_WEEKS,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_max15)
    public void onCheckedMax15SwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        WidgetConfig.apply(this,WidgetConfig.CONFIG_MAX_ITEM,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_hidedate)
    public void onCheckedHideDateSwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_DATE,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_alpha1)
    public void onAlpha1SwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        WidgetConfig.apply(this,WidgetConfig.CONFIG_ALPHA1,b);
        BroadcastUtils.refreshAppWidget(this);
        if(effect){
            ToastTools.show(context,"设置成功，重新添加小部件生效");
        }
    }

    @OnCheckedChanged(R.id.id_widget_textcolor)
    public void onWhiteTextColorSwitchClicked(boolean b) {
        if(effect&&!checkVipStatus()){
            return;
        }
        WidgetConfig.apply(this,WidgetConfig.CONFIG_TEXT_COLOR_WHITE,b);
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
                .setCancelable(false)
                .setPositiveButton("我知道了",null)
                .create();
        alertDialog.show();
    }

    public void setChangeStatus(boolean changeStatus) {
        if(effect){
            this.changeStatus = changeStatus;
        }
    }

    public void setChangeStatus2(boolean changeStatus2) {
        if(effect){
            this.changeStatus2 = changeStatus2;
        }
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
        if(!checkVipStatus()){
            return;
        }
        String[] items={
                "红色主题",
                "蓝色主题",
                "黑色主题",
                "紫色主题",
                "棕色主题",
                "青色主题",
                "蓝色主题2",
                "橙色主题",
                "灰色主题"
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

    @OnClick(R.id.id_set_theme_layout_normal)
    public void onSetThemeLayoutClicked2(){
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

    @OnClick(R.id.id_find_license)
    public void toFindLicenseActivity() {
        String deviceId=DeviceTools.getDeviceId(context);
        if(TextUtils.isEmpty(deviceId)){
            showDialog("权限不足","无法获取设备ID，证书验证失败，请先开启读取设备IMEI权限");
            return;
        }

        ActivityTools.toActivityWithout(getContext(),FindVipLicenseActivity.class);
    }

    @OnClick(R.id.id_check_license)
    public void checkLicense() {
        VipVerifyResult result=VipTools.isVip(getContext());
        if(result==null){
            showDialog("检查证书安全性","证书不存在");
            return;
        }
        if(result.isSuccess()){
            PayLicense license=result.getLicense();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            showDialog("检查证书安全性","您的证书状态安全，证书内容如下:\n"
            +"签发时间:"+sdf.format(new Date(Long.parseLong(license.getCreate())))+"\n"
            +"失效时间:"+sdf.format(new Date(Long.parseLong(license.getExpire())))+"\n"
            +"设备信息:"+DeviceTools.getDeviceId(this)+"\n"
            +"数字签名:"+license.getSignature2());
        }else if(result.isNeedVerify()){
            checkVip();
        }else{
            showDialog("检查证书安全性","验证失败:"+result.getMsg());
        }
    }

    @OnClick(R.id.id_export_calender)
    public void exportToSystemCalender() {
        if(!checkVipStatus()){
            return;
        }
        String startTimeString="";
        String startTime=ShareTools.getString(this,ShareConstants.STRING_START_TIME,null);
        if (TextUtils.isEmpty(startTime)) {
            showDialog("出现错误","开学时间或当前周未设置");
            return;
        }else{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2=new SimpleDateFormat("yyyy/MM/dd");
            try{
                Date date=sdf.parse(startTime);
                startTimeString=sdf2.format(date);
            }catch (Exception e){
                BuglyLog.e("MenuActivity",e.getMessage(),e);
            }
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("导出到日历账户")
                .setCancelable(false)
                .setMessage("开学时间:"+startTimeString+"(第"+TimetableTools.getCurWeek(getContext())+"周)\n将当前课表写入系统日历事件中，写入日历后对当前课表进行的编辑操作将不会自动同步到日历中\n2.请务必授予日历权限\n3.请保证本地日期准确\n4.请保证开学时间或当前周设置准确")
                .setPositiveButton("选择日历账户", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doExport();
                    }
                }).setNegativeButton("取消导出",null);
        builder.create().show();
    }

    @OnClick(R.id.id_export_calender_qinglv)
    public void exportQinglvToSystemCalender() {
        if(!checkVipStatus()){
            return;
        }
        String startTimeString="";
        String startTime=ShareTools.getString(this,ShareConstants.STRING_START_TIME,null);
        if (TextUtils.isEmpty(startTime)) {
            showDialog("出现错误","开学时间或当前周未设置");
            return;
        }else{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2=new SimpleDateFormat("yyyy/MM/dd");
            try{
                Date date=sdf.parse(startTime);
                startTimeString=sdf2.format(date);
            }catch (Exception e){
                BuglyLog.e("MenuActivity",e.getMessage(),e);
            }
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("导出到日历账户")
                .setCancelable(false)
                .setMessage("开学时间:"+startTimeString+"(第"+TimetableTools.getCurWeek(getContext())+"周)\n1.将情侣课表写入系统日历事件中\n2.请务必授予日历权限\n3.请保证本地日期准确\n4.请保证开学时间或当前周设置准确\n5.导出的数据在事件名会标记[情侣]")
                .setPositiveButton("选择日历账户", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doExport2();
                    }
                }).setNegativeButton("取消导出",null);
        builder.create().show();
    }
    public void doExportOperator(int id, final boolean qinglv){
        final ScheduleName newName = DataSupport.find(ScheduleName.class, id);
        if (newName == null) return;

        final List<String> startTimeList=new ArrayList<>();
        final List<String> endTimeList=new ArrayList<>();
        boolean getTime=TimetableTools.getTimeList(getContext(),startTimeList,endTimeList);
        if(!getTime||startTimeList.size()==0||endTimeList.size()==0){
            showDialog("请先设置时间","导出到日历需要依赖课程时间，请先去设置课程时间");
            if(loadAlert!=null) loadAlert.hide();
            return;
        }

        final List<Map<String,String>> calenderIdList=CalendarReminderUtils.listCalendarAccount(getContext());
        final String[] items=new String[calenderIdList.size()];
        int i=0;
        if(calenderIdList==null||calenderIdList.size()==0){
            showDialog("日历账户不存在","系统不存在日历账户，且自动添加日历账户失败");
            if(loadAlert!=null) loadAlert.hide();
            return;
        }

        for(Map<String,String> map:calenderIdList){
            items[i]=map.get("name")+"\n("+map.get("account")+")";
            i++;
        }
        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                final List<TimetableModel> models = (List<TimetableModel>) t;
                final List<Schedule> scheduleList=ScheduleSupport.transform(models);
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext())
                        .setTitle("选择日历账户")
                        .setCancelable(false)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Integer calId=Integer.parseInt(calenderIdList.get(i).get("calId"));
                                loadAlert.setContent("正在获取课表信息..");
                                loadAlert.show();
                                addCalenderById(models,scheduleList,startTimeList,endTimeList,qinglv,calId,
                                        calenderIdList.get(i).get("name"));
                            }
                        });
                builder.create().show();
            }
        });
    }

    private void addCalenderById(final List<TimetableModel> models, final List<Schedule> scheduleList,
                                 final List<String> startTimeList, final List<String> endTimeList,
                                 final boolean qinglv,
                                 final Integer calId, final String account) {
        final int curWeek=TimetableTools.getCurWeek(getContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (models != null) {
                    if (models != null && models.size() != 0) {

                        final String originText="准备写入到"+account+"账户中\n";

                        final String origin="计算量较大，课程较多时需要耗费15s左右，请耐心等待..\n"
                                +"等待写入"+models.size()+"个课程\n";

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadAlert.setContent(originText+origin);
                            }
                        });

                        int max=1;
                        for(Schedule model:scheduleList){
                            int modelMax=model.getStart()+model.getStep()-1;
                            if(modelMax>max){
                                max=modelMax;
                            }
                        }

                        if(max>startTimeList.size()){
                            final int finalMax = max;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadAlert.hide();
                                    showDialog("请先设置时间","当前课程最大节次为:"+ finalMax+"\n课程时间节次:"+startTimeList.size()+
                                            "\n请务必保证课程时间可以覆盖到所有课程，所有导出会有遗漏");
                                }
                            });
                            return;
                        }

                        int index=0;
                        for(Schedule model:scheduleList){
                            index++;
                            final int finalIndex = index;
                            final int finalIndex1 = index;
                            final boolean[] showError = {false};
                            CalendarReminderUtils.addScheduleToCalender(getContext(), calId,model,qinglv,
                                    startTimeList, endTimeList,
                                    curWeek, new OnExportProgressListener() {
                                        @Override
                                        public void onProgress(final int total, final int now) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadAlert.setContent(originText+origin+"写入第"+ finalIndex +"个数据("+now+"/"+total+")");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(final String msg) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(!showError[0]){
                                                        loadAlert.hide();
                                                        showDialog("导出失败","导出遇到了问题:"+msg);
                                                        showError[0] =true;
                                                    }
                                                }
                                            });
                                        }
                                    });
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadAlert.hide();
                                showDialog("导出到日历","导出成功，以后可以用日历看课表啦~");
                            }
                        });
                    } else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadAlert.hide();
                                ToastTools.show(getContext(),"导出失败：当前课表为空");
                            }
                        });
                    }
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadAlert.hide();
                            ToastTools.show(getContext(),"导出失败：本地课程读取失败");
                        }
                    });
                }
            }
        }).start();
    }

    public void doExport(){
        if(getContext()==null) return;
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            ShareTools.put(this, ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
        }

        loadAlert=new MessageAlert(getContext(),true).create();
        int id = ScheduleDao.getApplyScheduleId(this);
        doExportOperator(id,false);
    }

    public void doExport2(){
        int id2 = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID2, 0);
        if(id2==0){
            showDialog("导出失败","你还没有关联课表,先去和Ta的课表关联起来吧");
            return;
        }
        final ScheduleName newName = DataSupport.find(ScheduleName.class, id2);
        if (newName == null){
            showDialog("导出失败","关联的课表不存在,先去和Ta的课表关联起来吧");
            return;
        }
        loadAlert=new MessageAlert(getContext(),true).create();
        doExportOperator(id2,true);
    }

    @OnClick(R.id.id_clear_calender)
    public void clearSystemCalender() {
        if(!checkVipStatus()){
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("清除日历课程内容")
                .setMessage("该操作仅仅会清除日历中由怪兽课表添加的内容!")
                .setPositiveButton("开始清除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doCleat();
                    }
                }).setNegativeButton("取消清除",null);
        builder.create().show();
    }

    @OnClick(R.id.id_set_time)
    public void setTimeLayoutClicked() {
        if(!checkVipStatus()){
            return;
        }
        ActivityTools.toActivityWithout(getContext(),SetTimeActivity.class);
    }

    private void doCleat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadAlert=new MessageAlert(getContext(),true).create();
                        loadAlert.setContent("正在清除\n这可能需要一些时间，请耐心等待..");
                        loadAlert.show();
                    }
                });

                CalendarReminderUtils.deleteCalendarSchedule(getContext(), handler, new OnExportProgressListener() {
                    @Override
                    public void onProgress(final int total, final int now) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadAlert.setContent("正在清除\n这可能需要一些时间，请耐心等待..\n"+
                                "正在过滤..("+now+"/"+total+")");
                            }
                        });
                    }

                    @Override
                    public void onError(String msg) {

                    }
                });
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(loadAlert!=null) loadAlert.hide();
                    }
                });
            }
        }).start();
    }
    public boolean checkVipStatus(){
        String deviceId=DeviceTools.getDeviceId(context);
        if(TextUtils.isEmpty(deviceId)){
            showDialog("权限不足","无法获取设备ID，证书验证失败，请先开启读取设备IMEI权限");
            return false;
        }
        VipVerifyResult result=VipTools.isVip(this);
        if(result.isSuccess()){
            return true;
        }
        if(result.isNeedVerify()){
            showDialog("验证高级版","证书存在风险，需要连接网络进行验证！");
            checkVip();
            return false;
        }
        VipTools.showAlertDialog(this);
        return false;
    }
}
