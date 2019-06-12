package com.zhuangfei.hputimetable.event;

import com.zhuangfei.hputimetable.model.GreenFruitSchool;

/**
 * Created by Liu ZhuangFei on 2019/6/12.
 */
public class SelectSchoolEvent {
    private GreenFruitSchool school;

    public SelectSchoolEvent(GreenFruitSchool school) {
        this.school = school;
    }

    public void setSchool(GreenFruitSchool school) {
        this.school = school;
    }

    public GreenFruitSchool getSchool() {
        return school;
    }
}
