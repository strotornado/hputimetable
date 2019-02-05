package com.zhuangfei.hputimetable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.ShareModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.fragment.FuncFragment;
import com.zhuangfei.hputimetable.adapter.MyFragmentPagerAdapter;
import com.zhuangfei.hputimetable.fragment.HomeFragment;
import com.zhuangfei.hputimetable.fragment.ScheduleFragment;
import com.zhuangfei.hputimetable.fragment.ServiceStationFragment;
import com.zhuangfei.hputimetable.fragment.ThemeMarketFragment;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.UpdateTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnNoticeUpdateListener,OnSwitchPagerListener, OnUpdateCourseListener {

    private static final String TAG = "MainActivity";

    final int SUCCESSCODE = 1;

    ViewPager mViewPager;
    List<Fragment> mFragmentList;
    MyFragmentPagerAdapter mAdapter;

    OnSwitchTableListener onSwitchTableListener;
    OnUpdateCourseListener onUpdateCourseListener;
    OnNoticeUpdateListener onNoticeUpdateListener;

    public static final int REQUEST_IMPORT = 1;

    int toItem=0;

    @BindView(R.id.id_bind_school_layout)
    LinearLayout bindSchoolLayout;

    @BindView(R.id.id_school_edit)
    EditText schoolEdit;

    Animation upAnim,downAnim;

    @BindView(R.id.id_title_tab1)
    TextView tabTitle1;

    @BindView(R.id.id_title_tab2)
    TextView tabTitle2;

    @BindView(R.id.id_title_nav)
    View titleNavView;//指示条

    RelativeLayout.LayoutParams lp;
    float leftStart=0;
    float leftEnd=0;
    int normalTextSize=15;
    int highlighTextSize=20;
    int navWidthDip=16;//指示条宽度
    int titleWidthDip=40;//Tab宽度
    int marLeftDip=10;//边距

    float lastTextSize=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ViewTools.setTransparent(this);
        ViewTools.setStatusTextGrayColor(this);
        ButterKnife.bind(this);
        inits();
        shouldcheckPermission();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x124);
            }
        }, 500);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Serializable serializable=intent.getSerializableExtra("model");
        if(serializable!=null){
            BundleModel model= (BundleModel) serializable;
            if(model!=null){
                toItem = (int) model.get("item",0);
                if(mViewPager.getCurrentItem()!=toItem) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0x125);
                        }
                    }, 100);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(onNoticeUpdateListener!=null){
            onNoticeUpdateListener.onUpdateNotice();
        }
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
            if(msg.what==0x123){
                getFromClip();
            }
            if(msg.what==0x124){
                try{
                    UpdateTools.checkUpdate(MainActivity.this,false);
                }catch (Exception e){}
                String schoolName=ShareTools.getString(MainActivity.this,ShareConstants.STRING_SCHOOL_NAME,null);
                if(schoolName==null){
                    doUpAnim();
                }
            }
            if(msg.what==0x125){
                select(toItem);
            }
        }
    };

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
////        super.onRestoreInstanceState(savedInstanceState);
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }

    private void inits() {
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            ShareTools.put(this, ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
        }

        mViewPager = findViewById(R.id.id_viewpager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: "+position+"&&&"+positionOffset+"&&&"+positionOffsetPixels);

//                marLeft=(int)((screenWidth/3)*(position+positionOffset));
//                tabBottomViewParams.setMargins(marLeft,0,0,0);
//                tabBottomView.setLayoutParams(tabBottomViewParams);

                //20dp:Tab宽度的一半，8dp:指示条宽度的一半
                float marLeft=leftStart+(leftEnd-leftStart)*(position+positionOffset);
                lp.setMargins((int) marLeft,0,0,0);
                titleNavView.setLayoutParams(lp);

//                float rato=normalTextSize+(highlighTextSize-normalTextSize)*(position+positionOffset);
//                float rato2=highlighTextSize-(highlighTextSize-normalTextSize)*(position+positionOffset);
//                if(positionOffset==0||positionOffset==1||Math.abs(positionOffset-1)<0.1f||
//                        Math.abs(positionOffset-0)<0.1f||Math.abs(rato-lastTextSize)>0.5f){
//                    tabTitle1.setTextSize(rato2);
//                    tabTitle2.setTextSize(rato);
//                    lastTextSize=rato;
//                }

//                tabTitle1.setTextSize(rato2);
//                tabTitle2.setTextSize(rato);
            }

            @Override
            public void onPageSelected(int position) {
               updateTabStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        upAnim= AnimationUtils.loadAnimation(this,R.anim.anim_bind_up);
        downAnim= AnimationUtils.loadAnimation(this,R.anim.anim_bind_down);

        lp= (RelativeLayout.LayoutParams) titleNavView.getLayoutParams();
        lp.width=ScreenUtils.dip2px(this,navWidthDip);
        lp.height=ScreenUtils.dip2px(this,3);
        leftStart=ScreenUtils.dip2px(MainActivity.this,titleWidthDip/2-navWidthDip/2);
        leftEnd=ScreenUtils.dip2px(MainActivity.this,titleWidthDip+marLeftDip+titleWidthDip/2-navWidthDip/2);

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FuncFragment());
        mFragmentList.add(new ScheduleFragment());
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        int item = (int) BundleTools.getInt(this, "item", 0);
        select(item);
    }

    /**
     * 绑定学校页面上滑动画
     */
    public void doUpAnim(){
        bindSchoolLayout.setVisibility(View.VISIBLE);
        upAnim.startNow();
    }

    /**
     * 绑定学校页面下滑动画
     */
    public void doDownAnim(){
        downAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bindSchoolLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        downAnim.startNow();
    }

    @OnClick(R.id.id_bind_button)
    public void onBindButtonClicked(){
        String school=schoolEdit.getText().toString();
        if(!TextUtils.isEmpty(school)&&!TextUtils.isEmpty(school.trim())){
            ShareTools.putString(this,ShareConstants.STRING_SCHOOL_NAME,school);
        }

        bindSchoolLayout.setVisibility(View.GONE);
//        doDownAnim();
    }

    public void getFromClip() {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        if (data != null) {
            if(data.getItemCount()>0){
                ClipData.Item item = data.getItemAt(0);
                if(item.getText()!=null){
                    String content = item.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        int index = content.indexOf("#");
                        if (index != -1 && content.indexOf("怪兽课表") != -1) {
                            if (content.length() > index + 1) {
                                String id = content.substring(index + 1);
                                showDialogOnImport(id);
                                clearClip();
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearClip() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", "");
        cm.setPrimaryClip(mClipData);
    }

    public void getValue(String id) {
        TimetableRequest.getValue(this, id, new Callback<ObjResult<ValuePair>>() {
            @Override
            public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                ObjResult<ValuePair> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        ValuePair pair = result.getData();
                        if (pair != null) {
                            onImportFromClip(pair);
                        } else {
                            Toasty.error(MainActivity.this, "PutValue:data is null").show();
                        }
                    } else {
                        Toasty.error(MainActivity.this, "PutValue:" + result.getMsg()).show();
                    }
                } else {
                    Toasty.error(MainActivity.this, "PutValue:result is null").show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
                Toasty.error(MainActivity.this, "Error:" + t.getMessage()).show();
            }
        });
    }

    private void showDialogOnImport(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("有人给你分享了课表，是否导入?")
                .setTitle("导入分享")
                .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getValue(id);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    public void onImportFromClip(ValuePair pair) {
        if (pair == null) return;
        ScheduleName newName = new ScheduleName();
        SimpleDateFormat sdf = new SimpleDateFormat("导入-HHmm");
        newName.setName(sdf.format(new Date()));
        newName.setTime(System.currentTimeMillis());
        newName.save();

        try {
            ShareModel shareModel = new Gson().fromJson(pair.getValue(), ShareModel.class);
            if (shareModel != null) {
                if (shareModel.getType() == ShareModel.TYPE_PER_TABLE) {
                    List<TimetableModel> list = shareModel.getData();
                    List<TimetableModel> finalList=new ArrayList<>();
                    if (list != null) {
                        for (TimetableModel m : list) {
                            TimetableModel model=new TimetableModel();
                            model.setScheduleName(newName);
                            model.setName(m.getName());
                            model.setTeacher(m.getTeacher());
                            model.setStep(m.getStep());
                            model.setDay(m.getDay());
                            model.setStart(m.getStart());
                            model.setWeeks(m.getWeeks());
                            model.setWeekList(m.getWeekList());
                            model.setRoom(m.getRoom());
                            model.setMajor(m.getMajor());
                            model.setTerm(m.getTerm());
                            finalList.add(model);
                        }
                        DataSupport.saveAll(finalList);
                        showDialogOnApply(newName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.success(this,"Error:"+e.getMessage()).show();
        }
    }

    public void select(int i) {
        int lightColor=Color.RED;
        titleNavView.setBackgroundColor(lightColor);
        updateTabStatus(i);
        switch (i) {
            case 0:
                mViewPager.setCurrentItem(0);
                break;
            case 1:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    public void updateTabStatus(int i) {
        initTabs();
        int lightColor=Color.RED;
        titleNavView.setBackgroundColor(lightColor);
        switch (i) {
            case 0:
                tabTitle1.setTextSize(highlighTextSize);
                lp.setMargins((int)leftStart,0,0,0);
                break;
            case 1:
                tabTitle2.setTextSize(highlighTextSize);
                lp.setMargins((int)leftEnd,0,0,0);
                break;
        }
    }

    private void initTabs() {
        int defaultColor=Color.BLACK;
        tabTitle1.setTextColor(defaultColor);
        tabTitle2.setTextColor(defaultColor);

        tabTitle1.setTextSize(normalTextSize);
        tabTitle2.setTextSize(normalTextSize);
    }

    private void showDialogOnApply(final ScheduleName name) {
        if (name == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("你导入的数据已存储在多课表[" + name.getName() + "]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

//                        if(onUpdateCourseListener!=null){
//                            onUpdateCourseListener.onUpdateData();
//                        }

                        if (onSwitchTableListener != null) {
                            onSwitchTableListener.onSwitchTable(name);
                            mViewPager.setCurrentItem(1);
                        }
                        if(onNoticeUpdateListener!=null){
                            onNoticeUpdateListener.onUpdateNotice();
                        }
                        BroadcastUtils.refreshAppWidget(MainActivity.this);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("稍后设置", null);
        builder.create().show();
    }

    private void shouldcheckPermission() {
        PermissionGen.with(MainActivity.this)
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
        //在这个方法中做一些权限申请成功的事情

    }

    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        ToastTools.show(this, "权限不足，运行中可能会出现故障!");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof OnSwitchTableListener) {
            onSwitchTableListener = (OnSwitchTableListener) fragment;
        }
        if (fragment instanceof OnUpdateCourseListener) {
            onUpdateCourseListener = (OnUpdateCourseListener) fragment;
        }
        if(fragment instanceof OnNoticeUpdateListener){
            onNoticeUpdateListener= (OnNoticeUpdateListener) fragment;
        }
    }

    @Override
    public void onPagerSwitch() {
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUpdateData() {
        if (onUpdateCourseListener != null) {
            onUpdateCourseListener.onUpdateData();
        }
    }

    @Override
    public void onUpdateNotice() {
        if(onNoticeUpdateListener!=null){
            onNoticeUpdateListener.onUpdateNotice();
        }
    }

    public void getValue2(String id) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        final String s=sdf.format(new Date())+VersionTools.getVersionName();
        String store=ShareTools.getString(this,"app_update_info",null);
        int isIgnoreUpdate=ShareTools.getInt(this,"isIgnoreUpdate",0);
        if(isIgnoreUpdate==0&&(store==null||!store.equals(s))){
            TimetableRequest.getValue(this, id, new Callback<ObjResult<ValuePair>>() {
                @Override
                public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                    ObjResult<ValuePair> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            ValuePair pair = result.getData();
                            if (pair != null) {
                                try {
                                    String value = pair.getValue();
                                    String[] vals = value.split("#");
                                    if (vals.length >= 3) {
                                        int v = Integer.parseInt(vals[0]);
                                        int isIgnoreUpdate = ShareTools.getInt(MainActivity.this, "isIgnoreUpdate", 0);
                                        if (isIgnoreUpdate == 0 && v > VersionTools.getVersionNumber()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("发现新版本-v"+vals[1])
                                                    .setMessage("你可以在 工具箱->自动检查更新 中关闭提醒!\n\n更新日志:\n" + vals[2])
                                                    .setPositiveButton("去看看", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent intent = new Intent();
                                                            intent.setAction("android.intent.action.VIEW");
                                                            intent.setData(Uri.parse("https://www.coolapk.com/apk/com.zhuangfei.hputimetable"));
                                                            startActivity(intent);
                                                            if (dialogInterface != null) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("明天提醒", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            ShareTools.putString(MainActivity.this,"app_update_info",s);
                                                        }
                                                    });
                                            builder.create().show();
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
                }
            });
        }
    }

    @OnClick(R.id.id_title_tab1)
    public void onSelectedTab1(){
        select(0);
    }

    @OnClick(R.id.id_title_tab2)
    public void onSelectedTab2(){
        select(1);
    }

    @OnClick(R.id.id_search_school)
    public void toSearchSchool() {
        ActivityTools.toActivityWithout(this, SearchSchoolActivity.class);
    }
}
