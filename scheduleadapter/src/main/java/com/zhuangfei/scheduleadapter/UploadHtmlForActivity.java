package com.zhuangfei.scheduleadapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.scheduleadapter.openapis.IArea;
import com.zhuangfei.scheduleadapter.openapis.JsSupport;
import com.zhuangfei.scheduleadapter.webapis.TimetableRequest;
import com.zhuangfei.scheduleadapter.webapis.model.BaseResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 源码上传页面
 * 内部增加了对河南理工大学的兼容，不需要的话可以忽略
 */
public class UploadHtmlForActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    WebView webView;

    // 标题
    TextView titleTextView;
    String url, school;
    ImageView helpView;

    boolean isNeedLoad = false;
    JsSupport jsSupport;
    LinearLayout buttonGroupLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_html_for);
        initViews();
        initEvents();
        loadWebView();
    }

    private void initEvents() {
        findViewById(R.id.id_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        helpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopmenu();
            }
        });

        findViewById(R.id.iv_parseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onBtnClicked();
                buttonGroupLinear.setVisibility(View.VISIBLE);
            }
        });

        buttonGroupLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGroupLinear.setVisibility(View.GONE);
            }
        });
    }

    private void initViews() {
        webView=findViewById(R.id.id_webview);
        titleTextView=findViewById(R.id.id_web_title);
        helpView=findViewById(R.id.id_webview_help);
        buttonGroupLinear=findViewById(R.id.linear_buttongroup);

        url="http://www.liuzhuangfei.com";
        school="hpu";
    }

    /**
     * 显示弹出菜单
     */
    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this, helpView);
        popup.getMenuInflater().inflate(R.menu.menu_webview, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.top1){
                    String now=webView.getUrl();
                    if(now.indexOf("/")!=-1){
                        int index=now.lastIndexOf("/");
                        webView.loadUrl(now.substring(0,index)+"/xkAction.do?actionType=6");
                    }else{
                        webView.loadUrl(now+"/xkAction.do?actionType=6");
                    }
                }
                return true;
            }
        });
        popup.show();
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
                        showToast("上传源码成功，请等待开发者适配");
                    }else{
                        showToast("Error:"+result.getMsg());
                    }
                }else{
                    showToast("result is null!");
                }
                finish();
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                showToast("Exception:"+t.getMessage());
                finish();
            }
        });
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public void onBtnClicked() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("重要内容!")
                .setMessage("请在你看到课表后再点击此按钮!!!\n\n如果教务是URP，可能会出现点击无反应的问题，在该页面右上角选择URP-兼容模式即可")
                .setPositiveButton("看到了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isNeedLoad = true;
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
        else finish();
    }

    static class Params{

    }
}
