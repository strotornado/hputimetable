package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.toolkit.model.BundleModel;

/**
 * Created by Liu ZhuangFei on 2019/2/8.
 */
public class StationManager {
    public static void openStationWithout(Activity context,String url,String title){
        if(context==null) return;
        Intent intent=new Intent(context, StationWebViewActivity.class);
        Bundle bundle=new Bundle();
        BundleModel model=new BundleModel();
        model.setFromClass(context.getClass())
                .put(StationWebViewActivity.KEY_URL,url)
                .put(StationWebViewActivity.KEY_TITLE,title);
        bundle.putSerializable("model",model);
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.anim_station_open_activity,R.anim.anim_station_static);//动画
    }

    public static void openStation(Activity context,String url,String title){
        if(context==null) return;
        openStationWithout(context,url,title);
        context.finish();
    }
}
