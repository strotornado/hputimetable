package com.zhuangfei.hputimetable;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.AssetTools;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Arrays;
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

public class AdapterSchoolActivity extends AppCompatActivity {

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
    String url, school, js,type;

    boolean isParse = false;
    boolean isLoad=false;
    StringBuffer sb = new StringBuffer();
    String html = "";

    @BindView(R.id.id_webview_help)
    ImageView popmenuImageView;

    @BindView(R.id.id_loadingbar)
    ContentLoadingProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_school);
        ButterKnife.bind(this);
        initUrl();
        initView();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        school = BundleTools.getString(this, "school", "WebView");
        js = BundleTools.getString(this, "parsejs", null);
        type = BundleTools.getString(this, "type", null);
    }

    private void initView() {
        titleTextView.setText(school);
        closeLayout = (LinearLayout) findViewById(R.id.id_close);
        closeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this,
                        returnClass);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new SpecialArea(), "sa");
//        settings.setDefaultTextEncodingName("utf-8");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearFormData();

        settings.setSupportZoom(true);
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

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
            public void onProgressChanged(WebView view, final int newProgress) {
                super.onProgressChanged(view, newProgress);
                loadingProgressBar.setProgress(newProgress);
                if(newProgress==100) loadingProgressBar.hide();
                else loadingProgressBar.show();

                if (webView.getUrl().startsWith("https://vpn.hpu.edu.cn/web/1/http/1/218.196.240.97/loginAction.do")) {
                    webView.loadUrl("https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6");
                }
                if (newProgress > 60 && isParse) {
                    callJs("getTagList()");
                    isLoad=true;
                    isParse=false;
                }
            }
        });

    }

    public void callJs(String method) {
        Log.d(TAG, "callJs: " + method);
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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() && !isLoad) {
            webView.goBack();
        } else {
            ActivityTools.toBackActivityAnim(this, returnClass);
        }
    }

    public class SpecialArea {

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void forTagResult(final String[] tags) {
            final String[] finalTags = tags;
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (finalTags == null || finalTags.length == 0) {
                        Toasty.error(AdapterSchoolActivity.this, "Tag标签未设置").show();
                        ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this, returnClass);
                    } else if (finalTags.length == 1) {
                        callJs("parse('" + finalTags[0] + "')");
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdapterSchoolActivity.this);
                        builder.setTitle("请选择解析标签");
                        builder.setItems(finalTags, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                callJs("parse('" + finalTags[i] + "')");
                            }
                        });
                        builder.create().show();
                    }
                }
            });
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void forResult(String result) {
            final String finalResult = result;
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (finalResult == null) {
                        Toasty.error(AdapterSchoolActivity.this, "未发现匹配").show();
                        ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this, returnClass);
                    } else saveSchedule(finalResult);
                }
            });
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void error(final String msg) {
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(AdapterSchoolActivity.this, msg).show();
                }
            });
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void info(final String msg) {
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.info(AdapterSchoolActivity.this, msg).show();
                }
            });
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void warning(final String msg) {
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.warning(AdapterSchoolActivity.this, msg).show();
                }
            });
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public String getHtml() {
            return html;
        }

        @JavascriptInterface
        @SuppressLint("SetJavaScriptEnabled")
        public void showHtml(String content) {
            if (TextUtils.isEmpty(content)) return;
            html = content;
            AdapterSchoolActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parseHtml();
                }
            });
        }
    }

    public void saveSchedule(String data) {
        if (data == null) {
            Toasty.error(this, "error:data is null").show();
            ActivityTools.toBackActivityAnim(this, returnClass);
            return;
        }
        String[] items = data.trim().split("#");
        List<TimetableModel> models = new ArrayList<>();
        ScheduleName newName = new ScheduleName();
        newName.setName(school);
        newName.setTime(System.currentTimeMillis());
        newName.save();
        for (String item : items) {
            if (!TextUtils.isEmpty(item)) {
                String[] perItem = item.split("\\$");
                if (perItem == null || perItem.length < 7) continue;
                String name = perItem[0];
                String teacher = perItem[1];
                String weeks = perItem[2];
                String day = perItem[3];
                String start = perItem[4];
                String step = perItem[5];
                String room = perItem[6];
//
                int dayInt = Integer.parseInt(day);
                int startInt = Integer.parseInt(start);
                int stepInt = Integer.parseInt(step);

                String[] weeksArray = weeks.split(" ");
                List<Integer> weeksList = new ArrayList<>();
                for (String val : weeksArray) {
                    if (!TextUtils.isEmpty(val)) weeksList.add(Integer.parseInt(val));
                }
                TimetableModel model = new TimetableModel();
                model.setWeekList(weeksList);
                model.setTeacher(teacher);
                model.setStep(stepInt);
                model.setStart(startInt);
                model.setRoom(room);
                model.setName(name);
                model.setDay(dayInt);
                models.add(model);
                model.setScheduleName(newName);
            }
        }
        DataSupport.saveAll(models);
        Toasty.success(this, "保存成功！").show();
        showDialogOnApply(newName);
    }

    private void showDialogOnApply(final ScheduleName name) {
        if(name==null) return;
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        builder.setMessage("你导入的数据已存储在多课表["+name.getName()+"]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ScheduleDao.applySchedule(AdapterSchoolActivity.this,name.getId());
                        BroadcastUtils.refreshAppWidget(AdapterSchoolActivity.this);
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                        ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this, MainActivity.class,new BundleModel().put("item",1));
                    }
                })
                .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                        ActivityTools.toBackActivityAnim(AdapterSchoolActivity.this, MainActivity.class,new BundleModel().put("item",1));
                    }
                });
        builder.create().show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void parseHtml() {
        if (js != null) {
            String parseHtml = AssetTools.readAssetFile(this, "parse.html");
            parseHtml = parseHtml.replace("${jscontent}", js);
            webView.loadData(parseHtml, "text/html; charset=UTF-8", null);//这种写法可以正确解码
        }
    }

    @OnClick(R.id.cv_webview_parse)
    @SuppressLint("SetJavaScriptEnabled")
    public void onBtnClicked() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("重要内容!")
                .setMessage("请在你看到课表后再点击此按钮!!!")
                .setPositiveButton("看到了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isParse=true;
                        sb.setLength(0);
                        webView.loadUrl("javascript:var ifrs=document.getElementsByTagName(\"iframe\");" +
                                "var iframeContent=\"\";" +
                                "for(var i=0;i<ifrs.length;i++){" +
                                "iframeContent=iframeContent+ifrs[i].contentDocument.body.parentElement.outerHTML;" +
                                "}" +
                                "var frs=document.getElementsByTagName(\"frame\");" +
                                "var frameContent=\"\";" +
                                "for(var i=0;i<frs.length;i++){" +
                                "iframeContent=frameContent+frs[i].contentDocument.body.parentElement.outerHTML;" +
                                "}" +
                                "window.sa.showHtml(document.getElementsByTagName('html')[0].innerHTML + iframeContent+frameContent);");
                    }
                })
                .setNegativeButton("没看到", null);
        builder.create().show();
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
                                        .put("type",type)
                                        .put("js", js));
                        break;
                }
                return false;
            }
        });
        popup.show(); //这一行代码不要忘记了
    }
}
