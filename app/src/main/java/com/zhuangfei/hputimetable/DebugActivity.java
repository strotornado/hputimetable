package com.zhuangfei.hputimetable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.adapter_apis.IArea;
import com.zhuangfei.hputimetable.adapter_apis.JsSupport;
import com.zhuangfei.hputimetable.adapter_apis.ParseResult;
import com.zhuangfei.hputimetable.adapter_apis.SpecialArea;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.AdapterInfo;
import com.zhuangfei.hputimetable.api.model.HtmlDetail;
import com.zhuangfei.hputimetable.api.model.HtmlSummary;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 适配学校页面
 */
public class DebugActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    // wenview与加载条
    @BindView(R.id.id_webview)
    WebView webView;

    // 标题
    @BindView(R.id.id_web_title)
    TextView titleTextView;

    //右上角图标
    @BindView(R.id.id_webview_help)
    ImageView popmenuImageView;

    @BindView(R.id.cv_webview_parse)
    CardView parseCard;

    //加载进度
    @BindView(R.id.id_loadingbar)
    ContentLoadingProgressBar loadingProgressBar;

    // 解析课程相关
    JsSupport jsSupport;
    SpecialArea specialArea;
    String html = "";
    String school="", js, type="";
    String uid,aid;

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
        uid=getIntent().getStringExtra("uid");
        aid=getIntent().getStringExtra("aid");
        parseCard.setVisibility(View.GONE);
        if(uid==null||aid==null){
            ActivityTools.toBackActivityAnim(this,AdapterDebugTipActivity.class);
        }else{
            String filename=getIntent().getStringExtra("filename");
            titleTextView.setText(school);

            getHtml(filename);
        }
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
    }

    class MyWebViewCallback implements IArea.WebViewCallback {

        @Override
        public void onProgressChanged(int newProgress) {
            //进度更新
            loadingProgressBar.setProgress(newProgress);
            if (newProgress == 100) loadingProgressBar.hide();
            else loadingProgressBar.show();
        }
    }

    class MyCallback implements IArea.Callback {

        @Override
        public void onNotFindTag() {
            onError("Tag标签未设置");
            finish();
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
            finish();
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
        }
    }

    public Context context() {
        return DebugActivity.this;
    }

    public void saveSchedule(List<ParseResult> data) {
        if (data == null) {
            finish();
            return;
        }

        List<Schedule> models = new ArrayList<>();
        for (ParseResult item : data) {
            if (item == null) continue;
            Schedule model = new Schedule();
            model.setWeekList(item.getWeekList());
            model.setTeacher(item.getTeacher());
            model.setStep(item.getStep());
            model.setStart(item.getStart());
            model.setRoom(item.getRoom());
            model.setName(item.getName());
            model.setDay(item.getDay());
            models.add(model);
        }
        Intent intent=new Intent(this,DebugDisplayActivity.class);
        intent.putExtra("schedules",(Serializable) models);
        startActivity(intent);
        finish();
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
                        ActivityTools.toActivity(DebugActivity.this,
                                AdapterSameTypeActivity.class, new BundleModel()
                                        .put("type", type)
                                        .put("js", js));
                        break;
                    case R.id.id_menu2:
                        webView.loadUrl(URL_COURSE_RESULT);
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    public void getHtml(String filename){
        TimetableRequest.findHtmlDetail(this, filename, new Callback<ObjResult<HtmlDetail>>() {
            @Override
            public void onResponse(Call<ObjResult<HtmlDetail>> call, Response<ObjResult<HtmlDetail>> response) {
                ObjResult<HtmlDetail> result = response.body();
                if (result != null) {
                    HtmlDetail detail = result.getData();
                    if (result.getCode() == 200) {
                        html=detail.getContent();
                        getAdapterInfo(uid,aid);
                    } else {
                        Toasty.error(DebugActivity.this, result.getMsg()).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ObjResult<HtmlDetail>> call, Throwable t) {
                Toasty.error(DebugActivity.this, t.getMessage()).show();
            }
        });
    }

    public void getAdapterInfo(String uid,String aid){
        TimetableRequest.getAdapterInfo(this, uid, aid, new Callback<ObjResult<AdapterInfo>>() {
            @Override
            public void onResponse(Call<ObjResult<AdapterInfo>> call, Response<ObjResult<AdapterInfo>> response) {
                ObjResult<AdapterInfo> result = response.body();
                if (result != null) {
                    AdapterInfo info = result.getData();
                    if (result.getCode() == 200) {
                        jsSupport.parseHtml(context(),info.getParsejs());
                    } else {
                        Toasty.error(DebugActivity.this, result.getMsg()).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ObjResult<AdapterInfo>> call, Throwable t) {
                Toasty.error(DebugActivity.this, t.getMessage()).show();
            }
        });
    }
}
