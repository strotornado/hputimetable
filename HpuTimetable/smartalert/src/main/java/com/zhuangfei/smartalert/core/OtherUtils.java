package com.zhuangfei.smartalert.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class OtherUtils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
