package com.zhuangfei.hputimetable.station;

import android.widget.SimpleCursorTreeAdapter;

import com.zhuangfei.hputimetable.activity.StationWebViewActivity;

/**
 * Created by Liu ZhuangFei on 2019/2/6.
 */
public class StationSdk {
    StationWebViewActivity stationView;
    public StationSdk(StationWebViewActivity stationWebViewActivity){
        stationView=stationWebViewActivity;
    }
    public void relaseMemory(){
        stationView=null;
    }

    public void addButton(String btnText,String[] linkArray){
        stationView.setButtonSettings(btnText,linkArray);
    }
}
