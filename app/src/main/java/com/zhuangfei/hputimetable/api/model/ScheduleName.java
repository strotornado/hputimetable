package com.zhuangfei.hputimetable.api.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/17.
 */
public class ScheduleName extends DataSupport implements Serializable{

    List<TimetableModel> models=new ArrayList<>();

    public int id;

    private String name;

    private long time;

    private String desc;

    public List<TimetableModel> getModels() {
        return DataSupport.where("schedulename_id=?",String.valueOf(id)).find(TimetableModel.class,true);
    }

    public void setModels(List<TimetableModel> models) {
        this.models = models;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ScheduleName(String name, long time, String desc) {
        this.name = name;
        this.time = time;
        this.desc = desc;
    }

    public ScheduleName() {
    }
}
