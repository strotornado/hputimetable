package com.zhuangfei.hputimetable.specialarea;

import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/9/6.
 */
public class AreaNav {
    public static final int TYPE_CLICK=1;
    public static final int TYPE_MENU=2;

    private String text;
    private int type;
    private List<AreaNav> submenu;
    private String action;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<AreaNav> getSubmenu() {
        return submenu;
    }

    public void setSubmenu(List<AreaNav> submenu) {
        this.submenu = submenu;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
