package com.zhuangfei.hputimetable.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Liu ZhuangFei on 2019/6/12.
 */
public class PermissionTools {
    public static boolean hasPermission(Context context,String permission){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
}
