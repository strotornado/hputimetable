package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.content.Intent;

import com.zhuangfei.toolkit.tools.ShareTools;

/**
 * Created by Liu ZhuangFei on 2018/10/27.
 */
public class WidgetConfig {
    public static final String CONFIG_MAX_ITEM="CONFIG_MAX_ITEM";
    public static final String CONFIG_HIDE_WEEKS="CONFIG_HIDE_WEEKS";
    public static final String CONFIG_HIDE_DATE="CONFIG_HIDE_DATE";
    public static void apply(Context context,String config, boolean b){
        ShareTools.putInt(context,config,b==true?1:0);
    }

    public static boolean get(Context context,String config){
        return ShareTools.getInt(context,config,0)==1?true:false;
    }
}
