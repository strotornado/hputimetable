package com.zhuangfei.hputimetable.adapter;

import android.text.TextUtils;

import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.OnConfigHandleAdapter;

/**
 * Created by Liu ZhuangFei on 2019/1/22.
 */
public class OnGryphonConfigHandler extends OnConfigHandleAdapter {

    public static final String KEY_HIDE_NOT_CUR="config_hidenotcur";
    public static final String KEY_HIDE_WEEKENDS="config_hideweekends";
    public static final String VALUE_TRUE="value_true";
    public static final String VALUE_FALSE="value_false";

    @Override
    public void onParseConfig(String key, String value, TimetableView mView) {
        super.onParseConfig(key, value, mView);
        if(TextUtils.isEmpty(key)||TextUtils.isEmpty(value)||mView==null) return;

        switch (key){
            case KEY_HIDE_NOT_CUR:
                if(value.equals(VALUE_TRUE)){
                    mView.isShowNotCurWeek(false);
                }else{
                    mView.isShowNotCurWeek(true);
                }
                break;
            case KEY_HIDE_WEEKENDS:
                if(value.equals(VALUE_TRUE)){
                    mView.isShowWeekends(false);
                }else{
                    mView.isShowWeekends(true);
                }
                break;
        }
    }
}
