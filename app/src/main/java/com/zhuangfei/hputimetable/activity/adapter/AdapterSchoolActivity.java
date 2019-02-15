package com.zhuangfei.hputimetable.activity.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.adapter_apis.IArea;
import com.zhuangfei.hputimetable.adapter_apis.JsSupport;
import com.zhuangfei.hputimetable.adapter_apis.ParseResult;
import com.zhuangfei.hputimetable.adapter_apis.SpecialArea;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.ImportTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

/**
 * 适配学校页面
 */
public class AdapterSchoolActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    // wenview与加载条
    @BindView(R.id.id_webview)
    WebView webView;

    // 关闭
    private LinearLayout closeLayout;
    Class returnClass;//返回

    // 标题
    @BindView(R.id.id_web_title)
    TextView titleTextView;

    //右上角图标
    @BindView(R.id.id_webview_help)
    ImageView popmenuImageView;

    //加载进度
    @BindView(R.id.id_loadingbar)
    ContentLoadingProgressBar loadingProgressBar;

    // 解析课程相关
    JsSupport jsSupport;
    SpecialArea specialArea;
    String html = "";
    String url, school, js, type;

    //标记按钮是否已经被点击过
    //解析按钮如果点击一次，就不需要再去获取html了，直接解析
    boolean isButtonClicked=false;

    //选课结果
    public static final String URL_COURSE_RESULT="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_school);
        ButterKnife.bind(this);
        //init area
        initUrl();
        loadWebView();
    }

    /**
     * 获取参数
     */
    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        school = BundleTools.getString(this, "school", "WebView");
        js = BundleTools.getString(this, "parsejs", null);
        type = BundleTools.getString(this, "type", null);
        titleTextView.setText(school);
    }

    @OnClick(R.id.id_close)
    public void goBack() {
        ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this,
                returnClass);
    }

    /**
     * 核心方法:设置WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        jsSupport = new JsSupport(webView);
        specialArea = new SpecialArea(this, new MyCallback());
        jsSupport.applyConfig(this, new MyWebViewCallback());
        webView.addJavascriptInterface(specialArea, "sa");

        webView.loadUrl(url);
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else goBack();
    }

    class MyWebViewCallback implements IArea.WebViewCallback {

        @Override
        public void onProgressChanged(int newProgress) {
            //进度更新
            loadingProgressBar.setProgress(newProgress);
            if (newProgress == 100) loadingProgressBar.hide();
            else loadingProgressBar.show();

            //河南理工大学教务兼容性处理
            if (webView.getUrl().startsWith("https://vpn.hpu.edu.cn/web/1/http/1/218.196.240.97/loginAction.do")) {
                webView.loadUrl("https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6");
            }
        }
    }

    class MyCallback implements IArea.Callback {

        @Override
        public void onNotFindTag() {
            onError("Tag标签未设置");
            goBack();
        }

        @Override
        public void onFindTags(final String[] tags) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context());
            builder.setTitle("请选择解析标签");
            builder.setItems(tags, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    jsSupport.callJs("parse('" + tags[i] + "')");
                }
            });
            builder.create().show();
        }

        @Override
        public void onNotFindResult() {
            onError("未发现匹配");
            goBack();
        }

        @Override
        public void onFindResult(List<ParseResult> result) {
            saveSchedule(result);
        }

        @Override
        public void onError(String msg) {
            Toasty.error(context(), msg).show();
        }

        @Override
        public void onInfo(String msg) {
            Toasty.info(context(), msg).show();
        }

        @Override
        public void onWarning(String msg) {
            Toasty.warning(context(), msg).show();
        }

        @Override
        public String getHtml() {
            return html;
        }

        @Override
        public void showHtml(String content) {
            if (TextUtils.isEmpty(content)) {
                onError("showHtml:is Null");
            }
            html = content;
            jsSupport.parseHtml(context(),js);
        }
    }

    public Context context() {
        return AdapterSchoolActivity.this;
    }

    public void saveSchedule(List<ParseResult> data) {
        if (data == null) {
            ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this, returnClass);
            return;
        }

        //save
        List<TimetableModel> models = new ArrayList<>();
        ScheduleName newName = new ScheduleName();
        newName.setName(school);
        newName.setTime(System.currentTimeMillis());
        newName.save();
        for (ParseResult item : data) {
            if (item == null) continue;
            TimetableModel model = new TimetableModel();
            model.setWeekList(item.getWeekList());
            model.setTeacher(item.getTeacher());
            model.setStep(item.getStep());
            model.setStart(item.getStart());
            model.setRoom(item.getRoom());
            model.setName(item.getName());
            model.setDay(item.getDay());
            model.setScheduleName(newName);
            models.add(model);
        }
        DataSupport.saveAll(models);
        Toasty.success(this, "保存成功！").show();
        ImportTools.showDialogOnApply(this,newName);
    }

    @OnClick(R.id.cv_webview_parse)
    public void onBtnClicked() {
        if(!isButtonClicked){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("重要内容!")
                    .setMessage("请在你看到课表后再点击此按钮!!!\n\n如果教务是URP，可能会出现点击无反应的问题，在该页面右上角选择URP-兼容模式即可")
                    .setPositiveButton("看到了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isButtonClicked=true;
                            jsSupport.getPageHtml("sa");
                        }
                    })
                    .setNegativeButton("没看到", null);
            builder.create().show();
        }else jsSupport.parseHtml(context(),js);
    }

    @OnClick(R.id.id_webview_help)
    public void showPopMenu() {
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(this, popmenuImageView);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.adapter_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.id_menu1:
                        ActivityTools.toActivity(AdapterSchoolActivity.this,
                                AdapterSameTypeActivity.class, new BundleModel()
                                        .put("type", type)
                                        .put("js", js));
                        break;
                    case R.id.id_menu2:

                        String now=webView.getUrl();
                        if(now.indexOf("/")!=-1){
                            int index=now.lastIndexOf("/");
                            webView.loadUrl(now.substring(0,index)+"/xkAction.do?actionType=6");
                        }else{
                            webView.loadUrl(now+"/xkAction.do?actionType=6");
                        }

                        break;
                }
                return false;
            }
        });
        popup.show();
    }
}
