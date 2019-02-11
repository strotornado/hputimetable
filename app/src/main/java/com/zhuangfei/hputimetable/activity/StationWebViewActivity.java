package com.zhuangfei.hputimetable.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.adapter.UploadHtmlActivity;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.event.UpdateStationHomeEvent;
import com.zhuangfei.hputimetable.fragment.FuncFragment;
import com.zhuangfei.hputimetable.station.StationSdk;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 服务站加载引擎
 */
public class StationWebViewActivity extends AppCompatActivity {

    private static final String TAG = "StationWebViewActivity";

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

    // 声明PopupWindow
    private CustomPopWindow popupWindow;

    StationModel stationModel;
    public static final String EXTRAS_STATION_MODEL="station_model_extras";

    @BindView(R.id.id_station_root)
    LinearLayout rootLayout;

    List<StationModel> localStationModels;
    boolean haveLocal=false;
    int deleteId=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_web_view);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
        findStationLocal();
    }

    /**
     * 获取添加到首页的服务站
     */
    public void findStationLocal(){
        FindMultiExecutor findMultiExecutor=DataSupport.findAllAsync(StationModel.class);
        findMultiExecutor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<StationModel> stationModels= (List<StationModel>) t;
                if(localStationModels==null){
                    localStationModels=new ArrayList<>();
                }
                localStationModels.clear();
                localStationModels.addAll(stationModels);
                haveLocal=searchInList(localStationModels,stationModel.getStationId());
            }
        });
    }

    public boolean searchInList(List<StationModel> list,int stationId){
        if(list==null) return false;
        for(StationModel model:list){
            if(model.getStationId()==stationId){
                this.deleteId=model.getId();
                return true;
            }
        }
        return false;
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        stationModel= (StationModel) BundleTools.getObject(this,EXTRAS_STATION_MODEL,null);
        if(stationModel==null){
            ToastTools.show(this,"传参异常");
            finish();
        }
        url=stationModel.getUrl();
        title=stationModel.getName();
    }

    private void initView() {
        titleTextView.setText(title);
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pop_add_home:
                    if(haveLocal){
                        DataSupport.delete(StationModel.class,deleteId);
                        EventBus.getDefault().post(new UpdateStationHomeEvent());
                        ToastTools.show(StationWebViewActivity.this,"已从主页删除");
                    }else {
                        if(localStationModels.size()>=15){
                            ToastTools.show(StationWebViewActivity.this,"已达到最大数量限制15，请先删除其他服务站后尝试");
                        }else {
                            stationModel.save();
                            ToastTools.show(StationWebViewActivity.this,"已添加到首页");
                            EventBus.getDefault().post(new UpdateStationHomeEvent());
                        }
                    }
                    findStationLocal();
                    break;
                case R.id.pop_about:
                    if(stationModel!=null&&stationModel.getTag()!=null){
                        ToastTools.show(StationWebViewActivity.this,stationModel.getTag());
                    }else {
                        ToastTools.show(StationWebViewActivity.this,"标签未知!");
                    }
                    break;
                case R.id.pop_to_home:
                    webView.clearHistory();
                    webView.loadUrl(stationModel.getUrl());
                    break;
                default:
                    break;
            }
            popupWindow.dismiss();
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("gb2312");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new StationSdk(this,getStationSpace()), "sdk");

//        settings.setSupportZoom(true);
//        settings.setBuiltInZoomControls(true);

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
//                titleTextView.setText(title);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            back();
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

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.anim_station_static,R.anim.anim_station_close_activity);
    }

    /**
     * 弹出popupWindow更改头像
     */
    @OnClick(R.id.id_station_more)
    public void showMorePopWindow() {
        popupWindow = new CustomPopWindow(StationWebViewActivity.this,haveLocal, itemsOnClick);
        popupWindow.showAtLocation(rootLayout,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow.backgroundAlpha(StationWebViewActivity.this, 1f);
            }
        });
    }

    @OnClick(R.id.id_station_close)
    public void back(){
        finish();
    }

    public void showMessage(String msg){
        ToastTools.show(this,msg);
    }

    public Context getStationContext(){
        return this;
    }

    public WebView getWebView(){
        return webView;
    }

    public String getStationSpace(){
        return "station_space_"+stationModel.getStationId();
    }
}
