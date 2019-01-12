package com.zhuangfei.hputimetable.theme.core;

/**
 * Created by Liu ZhuangFei on 2019/1/11.
 */
public class ThemeLoader implements ITheme {
    /**
     * wakeup课程表模板
     */
    public static final String OPERATOR_WAKEUP="wakeup";

    /**
     * md课表模板
     */
    public static final String OPERATOR_MD="md";

    /**
     * utimetable课表模板
     */
    public static final String OPERATOR_UTIMETABLE="utimetable";

    /**
     * 南啊课表模板
     */
    public static final String OPERATOR_NANA="nana";

    /**
     * 轻课表模板
     */
    public static final String OPERATOR_QING="qing";

    /**
     * 超级课程表模板
     */
    public static final String OPERATOR_SUPER="super";

    /**
     * 课程格子模板
     */
    public static final String OPERATOR_CLASSBOX="classbox";

    /**
     * 默认模板
     */
    public static final String OPERATOR_DEFAULT="default";

    @Override
    public ThemeModel onParse(String themeJson) {
        return null;
    }

    @Override
    public void onLoad(ThemeModel themeModel) {
        if(themeModel==null) return;

    }
}
