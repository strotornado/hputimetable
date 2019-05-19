package com.zhuangfei.hputimetable.station;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.widget.SimpleCursorTreeAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.adapter_apis.JsSupport;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
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
    /**
     * @params firstUrl 重定向的地址
     */
    public void addButton(final String btnText, final String[] textArray, final String[] linkArray){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stationView.setButtonSettings(btnText,textArray,linkArray);
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void saveSchedules(String name,String json){
        Type type=new TypeToken<ObjResult<TimetableResultModel>>(){}.getType();
        ObjResult<TimetableResultModel> objResult=new Gson().fromJson(json,type);
        if(objResult!=null){
            if(objResult.getCode()==200){
                TimetableResultModel resultModel=objResult.getData();
                if(resultModel==null) return;
                List<TimetableModel> haveList=resultModel.getHaveList();
                List<TimetableModel> notimeList=resultModel.getNotimeList();
                if(haveList!=null){
                    ScheduleName newName=new ScheduleName();
                    newName.setName(name);
                    newName.setTime(System.currentTimeMillis());
                    newName.save();
                    for(TimetableModel model:haveList){
                        model.setScheduleName(newName);
                    }
                    if(notimeList!=null){
                        for(TimetableModel model:notimeList){
                            if(TextUtils.isEmpty(model.getName())){
                                model.setName("无时间课程,请自行修改");
                            }else{
                                model.setName(model.getName()+"(无时间课程,请自行修改)");
                            }
                            if(model.getDay()<1){
                                model.setDay(7);
                            }
                            if(model.getStart()<1){
                                model.setStart(1);
                            }
                            if(model.getStep()<1){
                                model.setStep(4);
                            }
                            if(model.getWeekList()==null||model.getWeekList().size()==0){
                                List<Integer> list=new ArrayList<>();
                                for(int i=1;i<=20;i++){
                                    list.add(i);
                                }
                                model.setWeekList(list);
                            }
                            model.setScheduleName(newName);
                        }
                    }

                    DataSupport.saveAll(haveList);
                    DataSupport.saveAll(notimeList);
                    ImportTools.showDialogOnApply(stationView.getStationContext(),newName);
                }else {
                    stationView.showMessage("haveList is null");
                }
            }else{
                toast("Error:"+objResult.getMsg());
            }
        }else{
            toast("Error:objResult is null");
        }
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void toast(final String msg){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stationView.showMessage(msg);
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void messageDialog(final String tag, final String title,
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
                                jsSupport.callJs("onMessageDialogCallback('$0')",new String[]{tag});
                            }
                        });
                builder.create().show();
            }
        });
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    public void setTitle(final String title){
        stationView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stationView.setTitle(title);
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
    public String getString(final String key, final String defVal){
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
                            String json=new Gson().toJson(allModels);
                            json=json.replaceAll("\"","&quot;");
                            messageDialog("tag","Title",json,"我知道了");
                            jsSupport.callJs("onGetCurrentScheduleCallback('$0')",new String[]{json});
                        }else{
                            jsSupport.callJs("onGetCurrentScheduleCallback('')",null);
                        }
                    }
                });
            }
        });
    }
}
