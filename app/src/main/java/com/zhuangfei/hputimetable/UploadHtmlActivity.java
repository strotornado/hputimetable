package com.zhuangfei.hputimetable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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

public class UploadHtmlActivity extends AppCompatActivity {

    public static final int MODE_GET_URL = 1;
    public static final int MODE_GET_HTML = 2;

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

    @BindView(R.id.id_display)
    TextView displayTextView;

    boolean isNeedLoad = false;
    List<String> frameList = new ArrayList<>();
    Queue<String> taskQueue = new LinkedList<>();

    @BindView(R.id.scrollview_display)
    ScrollView scrollView;

    StringBuffer sb = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_html);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        school = BundleTools.getString(this, "school", "WebView");
    }

    private void initView() {
        titleTextView.setText("适配-"+school);
        closeLayout = (LinearLayout) findViewById(R.id.id_close);
        closeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ActivityTools.toBackActivityAnim(UploadHtmlActivity.this,
                        returnClass);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new ShowSourceJs(), "source");
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

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                boolean isUseBrower = false;
                if (isUseBrower) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    webView.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                final int finalProgress = newProgress;
                UploadHtmlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (webView.getUrl().startsWith("https://vpn.hpu.edu.cn/web/1/http/1/218.196.240.97/loginAction.do")) {
                            webView.loadUrl("https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6");
                        }
                        if (finalProgress == 100 && isNeedLoad) {
                            webView.loadUrl("javascript:window.source.showHtml('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                        }
                    }
                });
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

    public class ShowSourceJs {
        private static final String TAG = "ShowSourceJs";

        @JavascriptInterface
        public void showHtml(final String content) {
            final String finalContent = content;
            if (TextUtils.isEmpty(content)) return;
            UploadHtmlActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sb.append("====Page====\n");
                    sb.append(finalContent);
                    sb.append("\n\n\n");

                    if (content.indexOf("<frame") != -1) {
                        Pattern pattern = Pattern.compile("<frame.*?src=\"(.*?)\".*?>");
                        final Matcher matcher = pattern.matcher(content);
                        while (matcher.find()) {
                            String src = matcher.group(1);
                            if (!frameList.contains(src)) {
                                frameList.add(src);
                                taskQueue.add(url + src);
                            }
                        }
                    }
                    //任务队列不空，开始执行第一个任务
                    if (!taskQueue.isEmpty()) webView.loadUrl(taskQueue.poll());
                    else {
                        displayTextView.setText(sb.toString());
                        putHtml(sb.toString());
                    }
                }
            });


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
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toasty.error(UploadHtmlActivity.this,t.getMessage()).show();
            }
        });
    }

    @OnClick(R.id.id_webview_code)
    @SuppressLint("SetJavaScriptEnabled")
    public void onBtnClicked() {
        displayTextView.setText("");
        frameList.clear();
        isNeedLoad = true;
        scrollView.setVisibility(View.VISIBLE);
        sb.setLength(0);
        webView.loadUrl("javascript:window.source.showHtml('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
    }
}
