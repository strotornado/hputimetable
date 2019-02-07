package com.zhuangfei.hputimetable.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.ImageUtil;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import es.dmoral.toasty.Toasty;

public class ScanActivity extends AppCompatActivity {

    public static final int REQUEST_SCAN = 2;
    public static final int REQUEST_OPEN_LOCAL = 10;
    private CaptureFragment captureFragment;
    private LinearLayout backLayout;
    private LinearLayout localLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();
        initEvent();
    }

    private void initView() {
        backLayout = (LinearLayout) findViewById(R.id.id_back);
        localLayout = (LinearLayout) findViewById(R.id.id_scan_local);

        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.view_scan_mycamera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_my_container, captureFragment).commit();
    }

    private void initEvent() {
        backLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ActivityTools.toBackActivityAnim(ScanActivity.this, MainActivity.class);
            }
        });

        localLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                openImage();
            }
        });
    }

    public void openImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_OPEN_LOCAL);
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            analyzeCode(result);
        }

        @Override
        public void onAnalyzeFailed() {
            Toasty.error(ScanActivity.this, "识别失败", Toast.LENGTH_SHORT)
                    .show();
            ActivityTools.toBackActivityAnim(ScanActivity.this, MainActivity.class);
        }
    };

    public void analyzeCode(String url) {
        if (SuperUtils.isSuperUrl(url)) {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_SCAN);
            intent.putExtra(AuthActivity.PARAMS_SCAN_URL, url);
            startActivityForResult(intent, REQUEST_SCAN);
        } else {
            Toast.makeText(this, "扫描的二维码不是超表课程码", Toast.LENGTH_SHORT).show();
            goBack();
        }
    }


    /**
     * 获取返回的数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SCAN && resultCode == AuthActivity.RESULT_STATUS) {
            SuperResult result=SuperUtils.getResult(data);
            displayLessons(result);
        }

        if (requestCode == REQUEST_OPEN_LOCAL) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            analyzeCode(result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Toasty.error(ScanActivity.this, "解析二维码失败").show();
                            goBack();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void displayLessons(SuperResult result) {
        if(result==null){
            Toasty.error(this, "result is null").show();
        }else if(result.getLessons()==null) {
            Toasty.error(this, "lessons is null").show();
        }else
        {
            if(result.isSuccess()){
                ScheduleName newName = ScheduleDao.saveSuperLessons(result.getLessons());
                if (newName != null) {
                    showDialogOnApply(newName);
                }
            }else{
                Toasty.error(this, ""+result.getErrMsg()).show();
            }
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
                        ScheduleDao.changeFuncStatus(ScanActivity.this,true);
                        ScheduleDao.applySchedule(ScanActivity.this,name.getId());
                        BroadcastUtils.refreshAppWidget(ScanActivity.this);
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                        ActivityTools.toBackActivityAnim(ScanActivity.this,
                                MainActivity.class,new BundleModel().put("item",1));
                    }
                })
                .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                        goBack();
                    }
                });
        builder.create().show();
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
