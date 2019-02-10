package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;

/**
 * Created by Liu ZhuangFei on 2019/2/6.
 */
public class StationModel implements Serializable{
    private String tag;
    private String name;
    private String img;
    private String url;
    private int id;
    private String owner;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}