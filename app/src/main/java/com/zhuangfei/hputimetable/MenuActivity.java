package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aigestudio.wheelpicker.widgets.WheelDatePicker;
import com.tencent.bugly.beta.Beta;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.specialarea.SpecialAreaActivity;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    Activity context;

    LinearLayout backLayout;

    public static final int REQUEST_IMPORT = 1;

    @BindView(R.id.id_switch_hidenotcur)
    SwitchCompat hideNotCurSwitch;

    @BindView(R.id.id_switch_mainalpha)
    SwitchCompat mainAlphaSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        context = this;
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

        int alpha = ShareTools.getInt(this, "mainalpha", 0);
        if (alpha == 0) {
            mainAlphaSwitch.setChecked(false);
        } else {
            mainAlphaSwitch.setChecked(true);
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

    @OnClick(R.id.id_menu_changeclass)
    public void changeClass() {
        ActivityTools.toActivity(this, ImportMajorActivity.class);
        finish();
    }

    @OnClick(R.id.id_menu_notice)
    public void onNoticeLayoutCLicked() {
        BundleModel model = new BundleModel();
        model.setFromClass(MenuActivity.class)
                .put("title", "最新公告")
                .put("url", "https://vpn.hpu.edu.cn")
                .put("isUse", 1);
        ActivityTools.toActivity(this, WebViewActivity.class, model);
        finish();
    }

    @OnClick(R.id.id_menu_score)
    public void score() {
        int show = ShareTools.getInt(this, ShareConstants.KEY_SHOW_ALERTDIALOG, 1);
        if (show == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("查询指南")
                    .setMessage("步骤如下：\n\n1.点击[确认]\n2.登录VPN,若失败,可以使用其他同学的校园网账号,vpn密码默认是身份证后六位" +
                            "\n3.登陆教务处,输入个人教务处账号,密码默认为学号\n4.登陆成功后,网页无法点击,这是正常现象." +
                            "\n4.此时,点击右上角,选择[兼容模式菜单],选择需要的功能即可\n");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BundleModel model = new BundleModel();
                    model.setFromClass(MenuActivity.class);
                    model.put("title", "成绩查询");
                    model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
                    ShareTools.putInt(MenuActivity.this, ShareConstants.KEY_SHOW_ALERTDIALOG, 0);
                    ActivityTools.toActivity(MenuActivity.this, WebViewActivity.class, model);
                }
            }).setNegativeButton("取消", null);
            builder.create().show();
        } else {
            BundleModel model = new BundleModel();
            model.setFromClass(MenuActivity.class);
            model.put("title", "成绩查询");
            model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
            ActivityTools.toActivity(MenuActivity.this, WebViewActivity.class, model);
        }
    }

    @OnClick(R.id.id_menu_about)
    public void about() {
        ActivityTools.toActivity(MenuActivity.this, AboutActivity.class);
        finish();
    }

    @OnClick(R.id.id_menu_issues)
    public void issues() {
        ActivityTools.toActivity(MenuActivity.this, WebViewActivity.class,
                new BundleModel().setFromClass(MenuActivity.class)
                        .put("title", "常见问题")
                        .put("url", "https://github.com/zfman/hputimetable/labels/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98"));
        finish();
    }

    @OnClick(R.id.id_menu_require_space)
    public void requireSpace() {
        ActivityTools.toActivity(MenuActivity.this, WebViewActivity.class,
                new BundleModel().setFromClass(MenuActivity.class)
                        .put("title", "专区申请")
                        .put("url", "https://github.com/zfman/hputimetable/wiki/%E5%AD%A6%E6%A0%A1%E4%B8%93%E5%8C%BA%E7%94%B3%E8%AF%B7%E7%96%91%E9%97%AE%E8%A7%A3%E7%AD%94"));
        finish();
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.id_menu_search)
    public void toSearchActivity() {
        ActivityTools.toActivity(getContext(), HpuRepertoryActivity.class);
        finish();
    }

    @OnClick(R.id.id_menu_schedule)
    public void toMultiScheduleActivity() {
        ActivityTools.toActivity(getContext(), MainActivity.class,new BundleModel().setToItem(2));
        finish();
    }

    @OnClick(R.id.id_menu_update)
    public void onUpdateLayoutClick() {
        Beta.checkUpgrade();
    }

    @OnClick(R.id.id_menu_scan)
    public void onScanLayoutClick() {
        ActivityTools.toActivity(getContext(), ScanActivity.class);
        finish();
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

    /**
     * 去授权<br/>
     * 在授权页面，会要求输入账号和密码，验证成功后会加载课程，
     * 并将加载的结果返回，你可以在onActivityResult对结果接收
     */
    @OnClick(R.id.id_menu_import)
    public void toAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_IMPORT);
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    @OnClick(R.id.id_menu_add)
    public void toAdd() {
//        BundleModel model = new BundleModel();
//        model.setFromClass(MenuActivity.class);
//        ActivityTools.toActivity(this, AddTimetableActivity.class, model);
//        finish();
        ActivityTools.toActivity(this, SpecialAreaActivity.class);
        finish();
    }

    /**
     * 接收授权页面获取的课程信息
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == AuthActivity.RESULT_STATUS) {
            SuperResult result=SuperUtils.getResult(data);
            if(result==null){
                Toasty.error(context, "result is null").show();
            }else{
                if(result.isSuccess()){
                    List<SuperLesson> lessons = result.getLessons();
                    ScheduleName newName = ScheduleDao.saveSuperShareLessons(lessons);
                    if (newName != null) {
                        Toasty.success(context, "已存储于[" + newName.getName() + "]").show();
                        ActivityTools.toActivity(this, MainActivity.class,new BundleModel().setToItem(2));
                        finish();
                    } else {
                        Toasty.error(context, "ScheduleName is null").show();
                    }
                }else{
                    Toasty.error(context, ""+result.getErrMsg()).show();
                }
            }

        }
    }

    @OnCheckedChanged(R.id.id_switch_hidenotcur)
    public void onHideNotCurSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "hidenotcur", 1);
        } else {
            ShareTools.putInt(this, "hidenotcur", 0);
        }
        ShareTools.putInt(this, "hidenotcur_changed", 1);
    }

    @OnCheckedChanged(R.id.id_switch_mainalpha)
    public void onMainAlphaSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "mainalpha", 1);
        } else {
            ShareTools.putInt(this, "mainalpha", 0);
        }
        ShareTools.putInt(this, "mainalpha_changed", 1);
    }
}
