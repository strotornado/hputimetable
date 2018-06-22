package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.constants.UrlContacts;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.OkHttpTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemClick;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    Activity context;

    LinearLayout backLayout;

    @BindView(R.id.id_show)
    ImageView imgShow;

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
    }

    public Activity getContext() {
        return context;
    }

    @OnClick(R.id.id_menu_clear)
    public void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空数据")
                .setMessage("确认后将删除本地保存的所有课程数据！请谨慎操作")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.clear(getContext());
                        DataSupport.deleteAll(TimetableModel.class);
                        Intent intent = new Intent(getContext(), SetMajorActivity.class);
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
        ShareTools.putString(getContext(), ShareConstants.KEY_MAJOR_NAME, "");
        Intent intent = new Intent(getContext(), SetMajorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getContext().startActivity(intent);
        getContext().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);//动画
        getContext().finish();
    }

    @OnClick(R.id.id_menu_score)
    public void score() {
        int show=ShareTools.getInt(this,ShareConstants.KEY_SHOW_ALERTDIALOG,1);
        if(show==1){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("查询指南")
                    .setMessage("步骤如下：\n\n1.点击[确认]\n2.登录VPN,若失败,可以使用其他同学的校园网账号，vpn密码默认是身份证后六位\n." +
                            "3.登陆教务处,输入个人教务处账号,密码默认为学号\n4.登陆成功后,网页无法点击,这是正常现象." +
                            "\n4.此时,点击右上角,选择[兼容模式菜单],选择需要的功能即可\n\n我只能帮你到这里了~~,如果感觉好用就帮我推广一下呗\n");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BundleModel model=new BundleModel();
                    model.setFromClass(MenuActivity.class);
                    model.put("title","成绩查询");
                    model.put("url","https://vpn.hpu.edu.cn/por/login_psw.csp");
                    ShareTools.putInt(MenuActivity.this,ShareConstants.KEY_SHOW_ALERTDIALOG,0);
                    ActivityTools.toActivity(MenuActivity.this,WebViewActivity.class,model);
                }
            }).setNegativeButton("取消", null);
            builder.create().show();
        }else{
            BundleModel model=new BundleModel();
            model.setFromClass(MenuActivity.class);
            model.put("title","成绩查询");
            model.put("url","https://vpn.hpu.edu.cn/por/login_psw.csp");
            ActivityTools.toActivity(MenuActivity.this,WebViewActivity.class,model);
        }
    }

    @OnClick(R.id.id_menu_issues)
    public void issues() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("问题反馈")
                .setMessage("您的任何的问题和建议都会列入我们的考虑范围之内.\n\n请加QQ群:684993074(hpu小课)\n\n如果感觉不好用的话私聊我，好用就帮我推广一下呗\n");

        builder.setPositiveButton("朕知道了,退下吧", null);
        builder.create().show();
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
        ActivityTools.toActivity(getContext(), SearchActivity.class);
    }

    @OnClick(R.id.id_menu_manager)
    public void toManagerActivity() {
        ActivityTools.toActivity(getContext(), TimetableManagerActivity.class);
    }

    @OnClick(R.id.id_menu_update)
    public void onUpdateLayoutClick() {
        Beta.checkUpgrade();
    }
}
