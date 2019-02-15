package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.toolkit.model.BundleModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Liu ZhuangFei on 2019/2/8.
 */
public class StationManager {
    public static void openStationWithout(Activity context,StationModel stationModel){
        if(context==null||stationModel==null) return;
        Intent intent=new Intent(context, StationWebViewActivity.class);
        Bundle bundle=new Bundle();
        BundleModel model=new BundleModel();
        model.setFromClass(context.getClass());
        model.put(StationWebViewActivity.EXTRAS_STATION_MODEL,stationModel);
        bundle.putSerializable("model",model);
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.anim_station_open_activity,R.anim.anim_station_static);//动画
    }

    public static void openStation(Activity context,StationModel stationModel){
        if(context==null) return;
        openStationWithout(context,stationModel);
        context.finish();
    }

    public static String getRealUrl(String url){
        if(TextUtils.isEmpty(url)) return null;
        int index=url.indexOf("#station_config#");//16 char
        if(index==-1){
            return url;
        }
        return url.substring(0,index);
    }

    public static Map<String,String> getStationConfig(String url){
        if(TextUtils.isEmpty(url)) return null;
        int index=url.indexOf("#station_config#");//16 char
        if(index==-1) return null;
        int nextIndex=index+"#station_config#".length();
        String config=url.substring(nextIndex);
        Map<String,String> map=new HashMap<>();
        if(!TextUtils.isEmpty(config)){
            String[] array=config.split("&");
            for(int i=0;i<array.length;i++){
                map.put(array[i].split("=")[0],array[i].split("=")[1]);
            }
            return map;
        }else {
            return null;
        }
    }
}
