package com.zhuangfei.hputimetable.event;

/**
 * Created by Liu ZhuangFei on 2019/2/16.
 */
public class UpdateSchoolEvent {
    private String school;

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public UpdateSchoolEvent() {
    }

    public UpdateSchoolEvent(String school) {
        this.school = school;
    }
}
