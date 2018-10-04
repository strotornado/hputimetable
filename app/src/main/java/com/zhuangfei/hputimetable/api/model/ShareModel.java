package com.zhuangfei.hputimetable.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/9/10.
 */
public class ShareModel implements Serializable{
    public static final int TYPE_PER_TABLE=1;
    private int type=1;
    List<TimetableModel> data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setData(List<TimetableModel> data) {
        this.data = data;
    }

    public List<TimetableModel> getData() {
        return data;
    }
}
