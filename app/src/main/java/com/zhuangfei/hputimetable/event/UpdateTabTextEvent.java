package com.zhuangfei.hputimetable.event;

/**
 * Created by Liu ZhuangFei on 2019/2/14.
 */
public class UpdateTabTextEvent {
    private String text;

    public UpdateTabTextEvent() {
    }

    public UpdateTabTextEvent(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
