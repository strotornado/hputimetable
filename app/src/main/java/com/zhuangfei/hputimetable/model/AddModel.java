package com.zhuangfei.hputimetable.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class AddModel {
    private int start=1;
    private int day=1;
    private int end=1;
    private String room="";

    public List<Boolean> status;

    public List<Boolean> getStatus() {
        if(status==null){
            status=new ArrayList<>();
            for(int i=0;i<25;i++){
                status.add(false);
            }
        }
        return status;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public void setStatus(List<Boolean> weeks) {
        this.status = weeks;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
