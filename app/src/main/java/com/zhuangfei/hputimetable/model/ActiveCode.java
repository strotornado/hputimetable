package com.zhuangfei.hputimetable.model;

import android.content.Context;
import android.text.TextUtils;

import com.zhuangfei.hputimetable.tools.AesSecurity;

/**
 * Created by Liu ZhuangFei on 2019/5/18.
 */
public class ActiveCode {
    private String newDeviceId;
    private String oldDeviceId;
    private boolean success=false;

    public boolean isSuccess() {
        return success;
    }

    public String getNewDeviceId() {
        return newDeviceId;
    }

    public void setNewDeviceId(String newDeviceId) {
        this.newDeviceId = newDeviceId;
    }

    public String getOldDeviceId() {
        return oldDeviceId;
    }

    public void setOldDeviceId(String oldDeviceId) {
        this.oldDeviceId = oldDeviceId;
    }

    public void load(String key,String encrypted){
        String source=AesSecurity.getInstance().decrypt(key,encrypted);
        if(TextUtils.isEmpty(source)){
            success=false;
            return;
        }
        if(source.indexOf("#")==-1){
            success=false;
            return;
        }
        String[] array=source.split("#");
        if(array==null||array.length<2){
            success=false;
            return;
        }
        setOldDeviceId(array[0]);
        setNewDeviceId(array[1]);
        success=false;
    }
}
