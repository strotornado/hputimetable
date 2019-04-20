package com.zhuangfei.hputimetable.tools;

import android.content.Context;
import android.text.TextUtils;

import com.payelves.sdk.EPay;
import com.payelves.sdk.listener.PayResultListener;
import com.payelves.sdk.listener.QueryOrderListener;
import com.zhuangfei.hputimetable.model.PayLicense;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static void checkPay(Context context, PayLicense license,QueryOrderListener listener){
        if(listener==null||context==null||license==null){
            listener.onFinish(false,"环境异常",null);
            return;
        }
        checkPaySdkInit(context);
        if(license==null){
            listener.onFinish(false,"证书校验失败",null);
            return;
        }
        EPay.getInstance(context).queryOrder(license.getOrderId(),listener);
    }

    public static String getOrderId(String userId,Integer amount){
        if(TextUtils.isEmpty(userId)||amount==0){
            return null;
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String orderid=sdf.format(new Date())+"_gs2_"+VersionTools.getVersionName()+"_"+userId+"_money"+amount;
        return orderid;
    }

    public static Integer getMoneyByOrderId(String orderId){
        if(orderId==null) return 0;
        //兼容旧版本
        if(orderId.indexOf("_money")==-1){
            return 880;
        }
        int index=orderId.lastIndexOf("_money");
        if(index==-1){
            return 880;
        }
        try {
            String moneyString=orderId.substring(index+6);
            Integer money=Integer.parseInt(moneyString);
            return money;
        }catch (Exception e){
            return 0;
        }
    }
}
