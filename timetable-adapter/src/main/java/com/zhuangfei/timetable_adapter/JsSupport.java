package com.zhuangfei.timetable_adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * Created by Liu ZhuangFei on 2018/10/27.
 */
public class JsSupport {
    public WebView webView;

    public JsSupport(@NonNull WebView webView){
        this.webView=webView;
    }

    public JsSupport setWebView(WebView webView) {
        this.webView = webView;
        return this;
    }

    /**
     * 调用一个函数，只负责调用，不对返回结果处理
     * @param method 方法名
     */
    public void callJs(String method) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            callEvaluateJavascript(method);
        } else { // 当Android SDK < 4.4时
            callMethod(method);
        }
    }

    /**
     * 4.4之下的调用js方法
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void callMethod(String method) {
        webView.loadUrl("javascript:" + method);
    }

    /**
     * 调用js方法（4.4之上）
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled")
    private void callEvaluateJavascript(String method) {
        // 调用html页面中的js函数
        webView.evaluateJavascript(method, null);
    }
}
