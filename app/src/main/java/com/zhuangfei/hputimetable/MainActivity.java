package com.zhuangfei.hputimetable;

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
import com.zhuangfei.hputimetable.fragment.FuncFragment;
import com.zhuangfei.hputimetable.adapter.MyFragmentPagerAdapter;
import com.zhuangfei.hputimetable.fragment.ScheduleFragment;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnSwitchPagerListener{

    private static final String TAG = "MainActivity";

    final int SUCCESSCODE = 1;

    ViewPager mViewPager;
    List<Fragment> mFragmentList;
    MyFragmentPagerAdapter mAdapter;

    OnSwitchTableListener onSwitchTableListener;

    public static final int REQUEST_IMPORT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        inits();
        shouldcheckPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        },300);
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getFromClip();
        }
    };

    private void inits() {
        mViewPager=findViewById(R.id.id_viewpager);

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FuncFragment());
        mFragmentList.add(new ScheduleFragment());
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        int item= (int) BundleTools.getInt(this,"item",0);
        select(item);
    }

    public void getFromClip(){
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        if(data!=null){
            ClipData.Item item = data.getItemAt(0);
            String content = item.getText().toString();
            if(!TextUtils.isEmpty(content)){
                int index=content.indexOf("#");
                if(index!=-1&&content.indexOf("怪兽课表")!=-1){
                    if(content.length()>index+1){
                        String id=content.substring(index+1);
                        showDialogOnImport(id);
                        clearClip();
                    }
                }
            }
        }
    }

    public void clearClip(){
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", "");
        cm.setPrimaryClip(mClipData);
    }

    public void getValue(String id){
        TimetableRequest.getValue(this, id, new Callback<ObjResult<ValuePair>>() {
            @Override
            public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                ObjResult<ValuePair> result=response.body();
                if(result!=null){
                    if(result.getCode()==200){
                        ValuePair pair=result.getData();
                        if(pair!=null){
                            onImportFromClip(pair);
                        }else{
                            Toasty.error(MainActivity.this,"PutValue:data is null").show();
                        }
                    }else{
                        Toasty.error(MainActivity.this,"PutValue:"+result.getMsg()).show();
                    }
                }else{
                    Toasty.error(MainActivity.this,"PutValue:result is null").show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
                Toasty.error(MainActivity.this,"Error:"+t.getMessage()).show();
            }
        });
    }

    private void showDialogOnImport(final String id) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("有人给你分享了课表，是否导入?")
                .setTitle("导入分享")
                .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getValue(id);
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("取消",null);
        builder.create().show();
    }

    public void onImportFromClip(ValuePair pair){
        if(pair==null) return;
        ScheduleName newName=new ScheduleName();
        SimpleDateFormat sdf=new SimpleDateFormat("导入-HHmm");
        newName.setName(sdf.format(new Date()));
        newName.setTime(System.currentTimeMillis());
        newName.save();

        try{
            ShareModel shareModel=new Gson().fromJson(pair.getValue(), ShareModel.class);
            if(shareModel!=null){
                if(shareModel.getType()==ShareModel.TYPE_PER_TABLE){
                    List<TimetableModel> list=shareModel.getData();
                    if(list!=null){
                        for(TimetableModel lesson:list){
                            lesson.setScheduleName(newName);
                            lesson.save();
                        }
                        DataSupport.saveAll(list);
                        showDialogOnApply(newName);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void select(int i){
        if(i>1) i=1;
        switch (i){
            case 0:
                mViewPager.setCurrentItem(0);
                break;
            case 1:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private void showDialogOnApply(final ScheduleName name) {
        if(name==null) return;
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("你导入的数据已存储在多课表["+name.getName()+"]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(onSwitchTableListener!=null){
                            onSwitchTableListener.onSwitchTable(name);
                            mViewPager.setCurrentItem(1);
                        }
                        BroadcastUtils.refreshAppWidget(MainActivity.this);
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("稍后设置",null);
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
        if(fragment instanceof OnSwitchTableListener){
            onSwitchTableListener= (OnSwitchTableListener) fragment;
        }
    }

    @Override
    public void onPagerSwitch() {
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem()==1){
            mViewPager.setCurrentItem(0);
        }else{
            ActivityTools.toHome(this);
        }
    }
}
