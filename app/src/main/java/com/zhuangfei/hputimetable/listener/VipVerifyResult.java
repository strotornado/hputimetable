package com.zhuangfei.hputimetable.listener;

import com.zhuangfei.hputimetable.model.PayLicense;

/**
 * Created by Liu ZhuangFei on 2019/4/17.
 */
public class VipVerifyResult {
    private boolean success=false;
    private boolean needVerify=false;
    private String msg="";
    private IVipVerifyListener listener;
    private PayLicense license;

    public void setLicense(PayLicense license) {
        this.license = license;
    }

    public PayLicense getLicense() {
        return license;
    }

    public void setNeedVerify(boolean needVerify) {
        this.needVerify = needVerify;
    }

    public boolean isNeedVerify() {
        return needVerify;
    }

    public void listener(IVipVerifyListener listener) {
        this.listener = listener;
    }

    public IVipVerifyListener getListener() {
        return listener;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        if(msg==null) msg="";
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success&&getLicense()!=null;
    }


}
