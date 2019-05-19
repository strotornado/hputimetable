package com.zhuangfei.hputimetable.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.enums.EPayResult;
import com.payelves.sdk.listener.QueryOrderListener;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.MenuActivity;
import com.zhuangfei.hputimetable.activity.VipActivity;
import com.zhuangfei.hputimetable.listener.VipVerifyResult;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.io.File;
import java.util.Calendar;
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

    public static String getLastModifyMd5(Context context){
        final File licenseFile= FileTools.getVipLicenseFile();
        String lastModifyMd5=null;
        if(licenseFile!=null){
            long lastModify=licenseFile.lastModified();
            String key=context.getResources().getString(R.string.key);
            lastModifyMd5= Md5Tools.encrypBy(""+lastModify+key);
            return lastModifyMd5;
        }
        return null;
    }

    public static VipVerifyResult isVip(Context context){
        VipVerifyResult result=new VipVerifyResult();
        PayLicense license=getLocalLicense(context,result);
        if(license==null){
            recordMsg(result,false,"vip.license is empty");
            return result;
        }
        result.setLicense(license);
        isVip(context,license,result);
        return result;
    }

    public static VipVerifyResult isVip(Context context,PayLicense license){
        return isVip(context,license,null);
    }

    public static VipVerifyResult isVip(Context context,PayLicense license,VipVerifyResult r){
        VipVerifyResult result;
        if(r==null){
            result=new VipVerifyResult();
        }else{
            result=r;
        }
        if(license==null){
            recordMsg(result,false,"vip.license is empty");
            return result;
        }
        result.setLicense(license);

        String modifyMd5=getLastModifyMd5(context);
        String localLastModify=ShareTools.getString(context,"lastModify","");
        if(modifyMd5!=null&&localLastModify!=null){
            if(localLastModify==null||!localLastModify.equals(modifyMd5)){
                recordMsg(result,false,"未验证的状态，请连接网络进行验证");
                recordVerifyMsg(result,true,"未验证的状态，请连接网络进行验证");
                return result;
            }
            recordVerifyMsg(result,false,"");
        }else{
            recordMsg(result,false,"license file is not exist");
            return result;
        }
        boolean check=license.check(context,result);
        return result;
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

    public static PayLicense getLicense(Context context,long payId,Date create,Integer amount){
        Date expire=getExpireDate(create,amount);
        if(context==null||expire==null){
            return new PayLicense();
        }
        PayLicense license=new PayLicense();
        String key=context.getResources().getString(R.string.key);
        String userId=DeviceTools.getDeviceId(context);
        String createTime=""+create.getTime();
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
        return getLocalLicense(context,null);
    }

    public static PayLicense getLocalLicense(Context context,VipVerifyResult result){
        if(context==null){
            recordMsg(result,false,"context is null");
            return null;
        }
        String json=FileTools.readVipLicense();
        if(TextUtils.isEmpty(json)){
            recordMsg(result,false,"vip.license is empty");
            return null;
        }
        PayLicense license=null;
        try {
            license=new Gson().fromJson(json,PayLicense.class);
        }catch (Exception e){
            license=null;
        }
        if(license==null){
            recordMsg(result,false,"license parse error");
            return null;
        }
        return license;
    }

    public static void recordMsg(VipVerifyResult result,boolean success,String msg){
        if(result!=null){
            result.setSuccess(success);
            result.setMsg(msg);
        }
    }
    public static void recordVerifyMsg(VipVerifyResult result,boolean needVerify,String msg){
        if(result!=null){
            result.setNeedVerify(needVerify);
            result.setMsg(msg);
        }
    }

    /**
     * 订单不存在以及校验失败时返回false
     * @param isSuccess
     * @param msg
     * @param model
     * @return
     */
    public static boolean checkOrderResult(Context context,boolean isSuccess, String msg, QueryOrderModel model,PayLicense license){
        if(context==null) return false;
        String deviceId=DeviceTools.getDeviceId(context);
        if(TextUtils.isEmpty(deviceId)) return false;
        if(msg!=null&&msg.indexOf("订单不存在")!=-1){
            return false;
        }
        //todo 添加时间校验

        if(model!=null&&model.getPayStatus()!=null&&!model.getPayStatus().equals("SUCCESS")){
            return false;
        }
        if(model==null||model.getOrderId()==null){
            return false;
        }
        if(model.getOrderId().indexOf(deviceId)==-1&&
                model.getOrderId().indexOf(license.getUserId2())==-1){
            return false;
        }
        return true;
    }

    public static void showDeleteLicenseDialog(Context context) {
        VipTools.unregisterVip();
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle("高级版被撤销")
                .setCancelable(false)
                .setMessage("经过系统检测，您的高级版凭证未通过验证，可能原因如下:\n1.高级版到期，请及时续费\n2.证书未通过校验，订单可能未支付成功\n3.证书未通过校验，证书可能遭到恶意篡改或者使用的软件非正版\n4.如果支付时的设备号与找回时的设备号不同时，恢复证书会失败，需要联系开发者申请激活码\n如果仍有疑问，请联系客服进行申诉:1193600556@qq.com")
                .setPositiveButton("我知道了", null);
        builder.create().show();
    }

    /**
     * 增加套餐时在这里修改
     * @param create
     * @param amount
     * @return
     */
    public static Date getExpireDate(Date create,Integer amount){
        if(create==null||amount<=0) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(create);
        if(amount==880){
            cal.add(Calendar.YEAR, 1);
        }
        if(amount==330){
            cal.add(Calendar.DATE, 30*3);
        }
        Date date = cal.getTime();
        return date;
    }

}
