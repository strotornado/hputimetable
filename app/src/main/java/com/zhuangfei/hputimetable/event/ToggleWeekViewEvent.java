package com.zhuangfei.hputimetable.event;

/**
 * Created by Liu ZhuangFei on 2019/2/14.
 */
public class ToggleWeekViewEvent {
    private boolean show;

    public ToggleWeekViewEvent() {
    }

    public ToggleWeekViewEvent(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
