package com.zhuangfei.hputimetable.api.model;

import org.litepal.crud.DataSupport;

/**
 * Created by Liu ZhuangFei on 2019/5/19.
 */
public class TodoModel extends DataSupport{
    private int id=0;
    private String title=null;
    private long timestamp=0;
    private boolean finish=false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }
}
