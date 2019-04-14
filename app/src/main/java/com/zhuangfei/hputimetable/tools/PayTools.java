package com.zhuangfei.hputimetable.tools;

import android.content.Context;

import com.payelves.sdk.EPay;
import com.payelves.sdk.listener.PayResultListener;

/**
 * Created by Liu ZhuangFei on 2019/4/5.
 */
public class PayTools {
    public static final String APP_KEY="8088846770569217";
    public static final String APP_ChANNAL="default";
    public static final String TOKEN="ebb48e277b8f4bab9c0e4eb38ef90708";
    public static final String OPEN_ID="yQYoTuscF";

    public static boolean init=false;

    public static void checkPaySdkInit(Context context){
        if(!init){
            EPay.getInstance(context).init(OPEN_ID,TOKEN,APP_KEY,APP_ChANNAL);
            init=true;
        }
    }

    public static void callPay(Context context, String subject, String body, Integer amount,
                        String order, String userId,
                        String backParams, PayResultListener payResultListener){
        checkPaySdkInit(context);
        EPay.getInstance(context).pay(subject,body,amount,order,userId,backParams,payResultListener);
    }
}
