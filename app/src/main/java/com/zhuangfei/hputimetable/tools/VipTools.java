package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.listener.QueryOrderListener;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.activity.VipActivity;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.util.Date;

/**
 * Created by Liu ZhuangFei on 2019/4/14.
 */
public class VipTools {
    public static void registerVip(PayLicense license){
        if(license==null){
            return;
        }
        try {
            String json=new Gson().toJson(license);
            FileTools.writeVipLicense(json);
        }catch (Exception e){
        }
    }

    public static void unregisterVip(){
        FileTools.writeVipLicense("");
    }

    public static boolean isVip(Context context){
        PayLicense license=getLocalLicense(context);
        if(license==null){
            return false;
        }
        return license.check(context);
    }

    public static void showAlertDialog(final Activity context){
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle("开通高级版")
                .setMessage("本功能属于高级版，请先开通高级版")
                .setPositiveButton("去开通", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityTools.toActivityWithout(context, VipActivity.class);
                    }
                }).setNegativeButton("取消",null)
                .setCancelable(false);
        builder.create().show();
    }

    public static PayLicense getLicense(Context context,long payId,Date expire){
        if(context==null||expire==null){
            return null;
        }
        PayLicense license=new PayLicense();
        String key=context.getResources().getString(R.string.key);
        String userId=DeviceTools.getDeviceId(context);
        String createTime=""+System.currentTimeMillis();
        license.setOrderId(payId);
        license.setUserId(userId);
        license.setCreate(createTime);
        license.setExpire(""+expire.getTime());
        String appSignature=Md5Tools.encrypBy(DeviceTools.getSHA1Signature(context));
        license.setSignature(appSignature);
        license.setSignature2(license.signature(context));
        return license;
    }

    public static PayLicense getLocalLicense(Context context){
        if(context==null){
            return null;
        }
        String json=FileTools.readVipLicense();
        if(TextUtils.isEmpty(json)){
            return null;
        }
        PayLicense license=null;
        try {
            license=new Gson().fromJson(json,PayLicense.class);
        }catch (Exception e){
            license=null;
        }
        if(license==null){
            return null;
        }
        return license;
    }
}
