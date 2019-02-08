package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2019/2/7.
 */
public class MessageModel implements Serializable{
    private int id;
    private String from_device;
    private String content;
    private String time;
    private int isread=0;
    private int unreadId;
    private String target;

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setUnreadId(int unreadId) {
        this.unreadId = unreadId;
    }

    public int getUnreadId() {
        return unreadId;
    }

    public int getIsread() {
        return isread;
    }

    public void setIsread(int isread) {
        this.isread = isread;
    }

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
