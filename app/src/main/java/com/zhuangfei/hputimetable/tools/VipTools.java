package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.VipActivity;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

/**
 * Created by Liu ZhuangFei on 2019/4/14.
 */
public class VipTools {
    public static void registerVip(Context context){
        String salt=context.getResources().getString(R.string.key);
        String time=""+System.currentTimeMillis();
        String value=Md5Tools.encrypBy(time+salt);
        FileTools.writeVipInfo(time,value);
    }

    public static boolean isVip(Context context){
        String salt=context.getResources().getString(R.string.key);
        String time=FileTools.readVipInfo("time");
        if(TextUtils.isEmpty(time)){
            return false;
        }
        String value=FileTools.readVipInfo("value");
        if(TextUtils.isEmpty(value)){
            return false;
        }
        if(!value.equals(Md5Tools.encrypBy(time+salt))){
            return false;
        }

        return true;
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
}
