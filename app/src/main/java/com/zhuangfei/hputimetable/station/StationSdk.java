package com.zhuangfei.hputimetable.station;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.webkit.JavascriptInterface;
import android.widget.SimpleCursorTreeAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.adapter_apis.JsSupport;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.ImportTools;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2019/2/6.
 */
public class StationSdk {
    StationWebViewActivity stationView;
    StationJsSupport jsSupport;
    private int minSupport=1;
    private int sdkVersion=1;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    public StationSdk(StationWebViewActivity stationWebViewActivity,String space){
        stationView=stationWebViewActivity;
        jsSupport=new StationJsSupport(stationView.getWebView());
        preferences=stationWebViewActivity.getSharedPreferences(space, Context.MODE_PRIVATE);
        editor=preferences.edit();
        setSdkVersion(1);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
        if(sdkVersion<minSupport){
            stationView.showMessage("版本太低，不支持本服务站，请升级新版本!");
            stationView.finish();
        }
    }

    private void setSdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public int getSdkVersion() {
        return sdkVersion;
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public String getBindSchool() {
        String schoolName= ShareTools.getString(stationView, ShareConstants.STRING_SCHOOL_NAME,null);
        return schoolName;
    }

    public void relaseMemory(){
        stationView=null;
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void addButton(String btnText,String[] linkArray){
        stationView.setButtonSettings(btnText,linkArray);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void saveSchedules(String name,String json){
        Type type = new TypeToken<List<TimetableModel>>(){}.getType();
        List<TimetableModel> modelList=new Gson().fromJson(json,type);
        if(modelList!=null){
            ScheduleName newName=new ScheduleName();
            newName.setName(name);
            newName.setTime(System.currentTimeMillis());
            newName.save();
            for(TimetableModel model:modelList){
                model.setScheduleName(newName);
            }
            DataSupport.saveAllAsync(modelList);
            ImportTools.showDialogOnApply(stationView.getStationContext(),newName);
            EventBus.getDefault().post(new UpdateScheduleEvent());
            stationView.showMessage("已保存至本地");
        }else {
            stationView.showMessage("解析错误");
        }
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void toast(String msg){
        stationView.showMessage(msg);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void messageDialog(String tag, final String title,
                              final String content, final String confirmText){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder=new AlertDialog.Builder(stationView.getStationContext())
                        .setTitle(title)
                        .setMessage(content)
                        .setPositiveButton(confirmText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(dialogInterface!=null){
                                    dialogInterface.dismiss();
                                }
                                jsSupport.callJs("onMessageDialogCallback()");
                            }
                        });
                builder.create().show();
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void putString(final String key, final String value){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editor.putString(key,value);
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public String getString(String key, String defVal){
        return preferences.getString(key,defVal);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void commit(){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public int getCurWeek(){
        return TimetableTools.getCurWeek(stationView);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void getCurrentSchedule(){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = ScheduleDao.getApplyScheduleId(stationView);
                final ScheduleName newName = DataSupport.find(ScheduleName.class, id);
                if (newName == null) return;

                FindMultiExecutor executor = newName.getModelsAsync();
                executor.listen(new FindMultiCallback() {
                    @Override
                    public <T> void onFinish(List<T> t) {
                        List<TimetableModel> models = (List<TimetableModel>) t;
                        if (models != null) {
                            List<Schedule> allModels = ScheduleSupport.transform(models);
                            if (allModels != null&&allModels.size()!=0) {
                                int curWeek = getCurWeek();
                                Calendar c = Calendar.getInstance();
                                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                                dayOfWeek = dayOfWeek - 2;
                                if (dayOfWeek == -1) dayOfWeek = 6;
                                List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
                                list=ScheduleSupport.getColorReflect(list);
                                if(list==null) list=new ArrayList<>();
                                String json=new Gson().toJson(list);
                                jsSupport.callJs("onGetCurrentScheduleCallback('$0')",json);
                            }
                        }
                    }
                });
            }
        });
    }
}
