package com.zhuangfei.hputimetable;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.zhuangfei.classbox.SuperBox;
import com.zhuangfei.classbox.listener.OnSuperAuthAdapter;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.ImageUtil;
import com.zhuangfei.toolkit.tools.ActivityTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                ActivityTools.toBackActivityAnim(ScanActivity.this, MenuActivity.class);
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
            ActivityTools.toBackActivityAnim(ScanActivity.this, MenuActivity.class);
        }
    };

    public void analyzeCode(String url) {
        boolean hasLocal = SuperUtils.isHasLocalData(this);
        if (SuperUtils.isSuperUrl(url)) {
            //如果以经有本地数据了，直接请求
            //否则需要去输入一个账号
            if (hasLocal) {
                new SuperBox(this).request(url, new OnSuperAuthAdapter() {
                    @Override
                    public void onScanSuccess(List<SuperLesson> lessons) {
                        super.onScanSuccess(lessons);
                        displayLessons(lessons);
                    }

                    @Override
                    public void onError(String msg) {
                        super.onError(msg);
                        Toast.makeText(ScanActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
                        goBack();
                    }

                    @Override
                    public void onException(Throwable t) {
                        super.onException(t);
                        Toast.makeText(ScanActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        goBack();
                    }
                });
            } else {
                Intent intent = new Intent(this, AuthActivity.class);
                intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_SCAN);
                intent.putExtra(AuthActivity.PARAMS_SCAN_URL, url);
                startActivityForResult(intent, REQUEST_SCAN);
            }
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
            List<SuperLesson> lessons = SuperUtils.getLessons(data);
            displayLessons(lessons);
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

    public void displayLessons(List<SuperLesson> lessons) {
        ScheduleName newName = ScheduleDao.saveSuperLessons(lessons);
        if (newName != null) {
            Toasty.success(this, "已存储于[" + newName.getName() + "]").show();
            ActivityTools.toActivity(this, MultiScheduleActivity.class);
            finish();
        } else goBack();
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
