package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;

/**
 * Created by Liu ZhuangFei on 2019/2/9.
 */
public class SchoolPersonModel implements Serializable{
    private int count;
    private String school;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
