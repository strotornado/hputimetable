package com.zhuangfei.hputimetable.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 服务站加载引擎
 */
public class StationWebViewActivity extends AppCompatActivity {

    private static final String TAG = "StationWebViewActivity";
    public static final String KEY_URL="url";
    public static final String KEY_TITLE="title";

    // wenview与加载条
    @BindView(R.id.id_webview)
    WebView webView;

    Class returnClass;

    // 标题
    @BindView(R.id.id_web_title)
    TextView titleTextView;
    String url,title;

    @BindView(R.id.id_loadingbar)
    ContentLoadingProgressBar loadingProgressBar;

    @BindView(R.id.id_btn_function)
    TextView functionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_web_view);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, KEY_URL, "http://www.liuzhuangfei.com");
        title=BundleTools.getString(this,KEY_TITLE,"WebView");
    }

    private void initView() {
        titleTextView.setText(title);
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
                Log.d(TAG, "shouldOverrideUrlLoading: "+url);
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
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                loadingProgressBar.setProgress(newProgress);
                if(newProgress==100) loadingProgressBar.hide();
                else loadingProgressBar.show();
            }

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

    public void setButtonSettings(String text,String[] linkArray){
        functionButton.setText(text);
        functionButton.setVisibility(View.VISIBLE);
    }
}
