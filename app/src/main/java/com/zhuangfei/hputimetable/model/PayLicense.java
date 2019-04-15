package com.zhuangfei.hputimetable.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.Md5Tools;

/**
 * Created by Liu ZhuangFei on 2019/4/14.
 */
public class PayLicense {

    /**
     * orderId : 0001
     * userId : 0001
     * signature :
     * omd5 :
     * umd5 :
     * expire :
     * create :
     * tmd5 :
     */
    private int version=1;//证书版本，升级认证机制时升级该版本
    private long orderId;
    private int money=0;
    private String userId;
    private String signature;// app signature
    private String expire;
    private String create;//time
    private String signature2;//all signature

    public void setMoney(int money) {
        this.money = money;
    }

    public int getMoney() {
        return money;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setSignature2(String signature2) {
        this.signature2 = signature2;
    }

    public String getSignature2() {
        return signature2;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public boolean check(Context context){
        try{
            String signatureKey=context.getResources().getString(R.string.signature);
            String signature= DeviceTools.getSHA1Signature(context);
            //验证凭证签名是否为本应用签发
            if(signature==null||getSignature()==null||!getSignature().equals(Md5Tools.encrypBy(signature))){
                return false;
            }
            if(!signatureKey.equals(Md5Tools.encrypBy(signature))){
                return false;
            }
            String key=context.getResources().getString(R.string.key);
            String deviceId= DeviceTools.getDeviceId(context);
            if(deviceId==null||getUserId()==null){
                return false;
            }
            if(!deviceId.equals(getUserId())){
                return false;
            }
            if(getCreate()==null||getExpire()==null){
                return false;
            }
            if(Long.parseLong(getExpire())<Long.parseLong(getCreate())){
                return false;
            }

            StringBuffer sb=new StringBuffer();
            sb.append("orderId=").append(getOrderId())
                    .append("&userId=").append(getUserId())
                    .append("&create=").append(getCreate())
                    .append("&expire=").append(getExpire())
                    .append("&signature=").append(getSignature());
            if(!Md5Tools.encrypBy(sb.toString()+key).equals(getSignature2())){
                return false;
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String signature(Context context){
        String key=context.getResources().getString(R.string.key);
        StringBuffer sb=new StringBuffer();
        sb.append("orderId=").append(getOrderId())
                .append("&userId=").append(getUserId())
                .append("&create=").append(getCreate())
                .append("&expire=").append(getExpire())
                .append("&signature=").append(getSignature());
        return Md5Tools.encrypBy(sb.toString()+key);
    }
}
