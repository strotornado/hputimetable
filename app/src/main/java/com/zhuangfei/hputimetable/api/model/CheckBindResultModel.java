package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;

/**
 * Created by Liu ZhuangFei on 2019/2/10.
 */
public class CheckBindResultModel implements Serializable{
    private int isBind=0;
    private String school;

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public void setIsBind(int isBind) {
        this.isBind = isBind;
    }

    public int getIsBind() {
        return isBind;
    }
}
