package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by Liu ZhuangFei on 2019/2/7.
 */
public class DeviceTools {
    public static String getDeviceId(Context context){
        if(context==null) return null;
        String deviceId=null;
        try{
            TelephonyManager mgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mgr.getDeviceId() != null) {
                deviceId = mgr.getDeviceId();
            } else {
                //android.provider.Settings;
                deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }catch (SecurityException e){
        }
        return deviceId;
    }
}
