package com.zhuangfei.hputimetable.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TimetableResultModel{
    @SerializedName("havetime")
    private List<TimetableModel> haveList;

    @SerializedName("notime")
    private List<TimetableModel> notimeList;

    public List<TimetableModel> getNotimeList() {
        return notimeList;
    }

    public void setNotimeList(List<TimetableModel> notimeList) {
        this.notimeList = notimeList;
    }

    public List<TimetableModel> getHaveList() {
        return haveList;
    }

    public void setHaveList(List<TimetableModel> haveList) {
        this.haveList = haveList;
    }
}