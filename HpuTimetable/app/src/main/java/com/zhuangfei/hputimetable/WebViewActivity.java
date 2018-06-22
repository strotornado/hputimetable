package com.zhuangfei.hputimetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.bugly.crashreport.BuglyLog;
import com.zhuangfei.generalpage.BaseActivity;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewActivity extends AppCompatActivity {

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
    String url,title;

    boolean isScoreQuery=false;

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

    @BindView(R.id.id_webview_layout)
    LinearLayout layout;

    @BindView(R.id.id_webview_help)
    TextView helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        title=BundleTools.getString(this,"title","WebView");
        if(title!=null&&title.indexOf("成绩")!=-1) isScoreQuery=true;
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

    private void initView() {
        titleTextView.setText(title);
        closeLayout = (LinearLayout) findViewById(R.id.id_close);
        closeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ActivityTools.toBackActivityAnim(WebViewActivity.this,
                        returnClass);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("gb2312");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleTextView.setText(title);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            ActivityTools.toBackActivityAnim(this, returnClass);
        }

    }

    @Override
    protected void onDestroy() {
        if (webView!= null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView= null;
        }
        super.onDestroy();
    }
}
