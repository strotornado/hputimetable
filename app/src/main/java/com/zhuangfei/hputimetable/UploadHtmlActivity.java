package com.zhuangfei.hputimetable;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhuangfei.hputimetable.adapter_apis.IArea;
import com.zhuangfei.hputimetable.adapter_apis.JsSupport;
import com.zhuangfei.hputimetable.adapter_apis.SpecialArea;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 源码上传页面
 * 内部增加了对河南理工大学的兼容，不需要的话可以忽略
 */
public class UploadHtmlActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    // wenview与加载条
    @BindView(R.id.id_webview)
    WebView webView;

    // 关闭
    private LinearLayout closeLayout;
    Class returnClass;

    // 标题
    @BindView(R.id.id_web_title)
    TextView titleTextView;
    String url, school;

    @BindView(R.id.id_webview_help)
    ImageView helpView;

    boolean isNeedLoad = false;

    StringBuffer sb = new StringBuffer();

    @BindView(R.id.id_webview_layout)
    LinearLayout layout;

    //所有成绩
    public static final String URL_SCORE_ALL="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/gradeLnAllAction.do?type=ln&oper=qb";

    //本学期成绩
    public static final String URL_SCORE_TERM="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/bxqcjcxAction.do";

    //空教室
    public static final String URL_EMPTYROOM="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xszxcxAction.do?oper=xszxcx_lb";

    //选课
    public static final String URL_COURSE_CHOOSE="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do";

    //退课
    public static final String URL_COURSE_DELETE="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=7";

    //选课结果
    public static final String URL_COURSE_RESULT="https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6";


    JsSupport jsSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_html);
        ButterKnife.bind(this);
        initUrl();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        school = BundleTools.getString(this, "school", "WebView");
        titleTextView.setText("适配-"+school);
    }

    @OnClick(R.id.id_close)
    public void goBack() {
        ActivityTools.toBackActivityAnim(UploadHtmlActivity.this,
                returnClass);
    }

    /**
     * 显示弹出菜单
     */
    @OnClick(R.id.id_webview_help)
    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this, helpView);
        popup.getMenuInflater().inflate(R.menu.menu_webview, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.top1:
                        layout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.top2:
                        webView.loadUrl(url);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        popup.show();
    }


    @OnClick(R.id.id_webview_btn1)
    public void onButton1CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_SCORE_ALL);
    }

    @OnClick(R.id.id_webview_btn2)
    public void onButton2CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_SCORE_TERM);
    }

    @OnClick(R.id.id_webview_btn3)
    public void onButton3CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_EMPTYROOM);
    }

    @OnClick(R.id.id_webview_btn4)
    public void onButton4CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_CHOOSE);
    }

    @OnClick(R.id.id_webview_btn5)
    public void onButton5CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_DELETE);
    }

    @OnClick(R.id.id_webview_btn6)
    public void onButton6CLicked(){
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_RESULT);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        jsSupport=new JsSupport(webView);
        jsSupport.applyConfig(this,new MyWebViewCallback());
        webView.addJavascriptInterface(new ShowSourceJs(), "source");

        webView.loadUrl(url);
    }

    class MyWebViewCallback implements IArea.WebViewCallback {

        @Override
        public void onProgressChanged(int newProgress) {
            //河南理工大学教务兼容性处理
            if (webView.getUrl()!=null&&webView.getUrl().startsWith("https://vpn.hpu.edu.cn/web/1/http/1/218.196.240.97/loginAction.do")) {
                webView.loadUrl("https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6");
            }
        }
    }

    public class ShowSourceJs {
        @JavascriptInterface
        public void showHtml(final String content) {
            if (TextUtils.isEmpty(content)) return;
            putHtml(content);
        }
    }

    private void putHtml(String html) {
        TimetableRequest.putHtml(this, school, url, html, new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                BaseResult result=response.body();
                if(result!=null){
                    if(result.getCode()==200){
                        Toasty.success(UploadHtmlActivity.this,"上传源码成功，请等待开发者适配").show();
                    }else{
                        Toasty.error(UploadHtmlActivity.this,result.getMsg()).show();
                    }
                }else{
                    Toasty.error(UploadHtmlActivity.this,"result is null!").show();
                }
                ActivityTools.toBackActivityAnim(UploadHtmlActivity.this, returnClass);
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toasty.error(UploadHtmlActivity.this,t.getMessage()).show();
                ActivityTools.toBackActivityAnim(UploadHtmlActivity.this, returnClass);
            }
        });
    }

    @OnClick(R.id.cv_webview_code)
    public void onBtnClicked() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("重要内容!")
                .setMessage("请在你看到课表后再点击此按钮!!!")
                .setPositiveButton("看到了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isNeedLoad = true;
                        sb.setLength(0);
                        jsSupport.getPageHtml("source");
                    }
                })
                .setNegativeButton("没有看到", null);
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()&&!isNeedLoad)
            webView.goBack();
        goBack();
    }
}
