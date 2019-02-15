package com.zhuangfei.hputimetable.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.adapter.SearchSchoolActivity;
import com.zhuangfei.hputimetable.activity.adapter.UploadHtmlActivity;
import com.zhuangfei.hputimetable.adapter.SearchSchoolAdapter;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.event.ReloadStationEvent;
import com.zhuangfei.hputimetable.event.UpdateStationHomeEvent;
import com.zhuangfei.hputimetable.fragment.FuncFragment;
import com.zhuangfei.hputimetable.model.SearchResultModel;
import com.zhuangfei.hputimetable.station.StationSdk;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    @BindView(R.id.id_station_action_bg)
    LinearLayout actionbarLayout;

    Map<String,String> configMap;

    @BindView(R.id.iv_station_more)
    ImageView moreImageView;

    @BindView(R.id.iv_station_close)
    ImageView closeImageView;

    @BindView(R.id.id_station_buttongroup)
    LinearLayout buttonGroupLayout;

    @BindView(R.id.id_station_diver)
    View diverView;//分隔竖线

    int needUpdate=0;
    String[] textArray=null,linkArray=null;
    String tipText=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeSetContentView();
        setContentView(R.layout.activity_station_web_view);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
        findStationLocal();
        getStationById();
    }

    private void beforeSetContentView() {
        stationModel= (StationModel) BundleTools.getObject(this,EXTRAS_STATION_MODEL,null);
        if(stationModel==null){
            ToastTools.show(this,"传参异常");
            finish();
        }
        configMap= StationManager.getStationConfig(stationModel.getUrl());
        if(configMap!=null&&!configMap.isEmpty()){
            try{
                ViewTools.setStatusBarColor(this, Color.parseColor(configMap.get("statusColor")));
            }catch (Exception e){}
        }
    }

    public void getStationById(){
        if(needUpdate==0) return;
        TimetableRequest.getStationById(this, stationModel.getStationId(), new Callback<ListResult<StationModel>>() {
            @Override
            public void onResponse(Call<ListResult<StationModel>> call, Response<ListResult<StationModel>> response) {
                ListResult<StationModel> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        showStationResult(result.getData());
                    } else {
                        Toast.makeText(StationWebViewActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StationWebViewActivity.this, "station response is null!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<StationModel>> call, Throwable t) {

            }
        });
    }

    private void showStationResult(List<StationModel> result) {
        if (result == null||result.size()==0) return;
        final StationModel model=result.get(0);
        if(model!=null){
            boolean update=false;
            if(model.getName()!=null&&!model.getName().equals(stationModel.getName())){
                update=true;
            }
            if(model.getUrl()!=null&&!model.getUrl().equals(stationModel.getUrl())){
                update=true;
            }
            if(model.getImg()!=null&&!model.getImg().equals(stationModel.getImg())){
                update=true;
            }

            if(update){
                final StationModel local=DataSupport.find(StationModel.class,stationModel.getId());
                if(local!=null){
                    local.setName(model.getName());
                    local.setUrl(model.getUrl());
                    local.setImg(model.getImg());
                    local.update(stationModel.getId());
                }

                AlertDialog.Builder builder=new AlertDialog.Builder(this)
                        .setTitle("服务站更新")
                        .setMessage("本地保存的服务站已过期，需要重新加载")
                        .setPositiveButton("重新加载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ReloadStationEvent event=new ReloadStationEvent();
                                event.setStationModel(local);
                                EventBus.getDefault().post(new UpdateStationHomeEvent());
                                EventBus.getDefault().post(event);
                                finish();
                            }
                        });
                builder.create().show();
            }
        }
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
        url=StationManager.getRealUrl(stationModel.getUrl());
        title=stationModel.getName();
        if(returnClass==MainActivity.class){
            needUpdate=1;
        }else {
            needUpdate=0;
        }
    }

    private void initView() {
        titleTextView.setText(title);
        if(configMap!=null&&!configMap.isEmpty()){
            try{
                actionbarLayout.setBackgroundColor(Color.parseColor(configMap.get("actionColor")));
            }catch (Exception e){}

            try{
                int textcolor=Color.parseColor(configMap.get("actionTextColor"));
                titleTextView.setTextColor(textcolor);
                moreImageView.setColorFilter(textcolor);
                closeImageView.setColorFilter(textcolor);
                GradientDrawable gd=new GradientDrawable();
                gd.setCornerRadius(ScreenUtils.dip2px(this,25));
                gd.setStroke(2,textcolor);
                diverView.setBackgroundColor(textcolor);
                buttonGroupLayout.setBackgroundDrawable(gd);
            }catch (Exception e){}
        }
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
                    if(stationModel!=null&&stationModel.getOwner()!=null){
                        ToastTools.show(StationWebViewActivity.this,stationModel.getOwner());
                    }else {
                        ToastTools.show(StationWebViewActivity.this,"所有者未知!");
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

    public void setButtonSettings(String btnText,String[] textArray,String[] linkArray){
        if(TextUtils.isEmpty(btnText)) return;
        functionButton.setText(btnText);
        functionButton.setVisibility(View.VISIBLE);
        this.textArray=textArray;
        this.linkArray=linkArray;
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

    public void setTitle(String title){
        titleTextView.setText(title);
    }

    @OnClick(R.id.id_btn_function)
    public void onButtonClicked(){
        if(textArray==null||linkArray==null) return;
        if(textArray.length!=linkArray.length) return;
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("请选择功能")
                .setItems(textArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i<linkArray.length){
                            webView.loadUrl(linkArray[i]);
                        }
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.create().show();
    }
}
