package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Liu ZhuangFei on 2019/2/7.
 */
public class DeviceTools {
    public static String getDeviceId(Context context){
        if(context==null) return null;
        TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }
}
