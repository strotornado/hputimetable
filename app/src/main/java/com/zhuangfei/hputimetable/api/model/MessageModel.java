package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;

/**
 * Created by Liu ZhuangFei on 2019/2/7.
 */
public class MessageModel implements Serializable{
    private int id;
    private String from_device;
    private String content;
    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom_device() {
        return from_device;
    }

    public void setFrom_device(String from_device) {
        this.from_device = from_device;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
