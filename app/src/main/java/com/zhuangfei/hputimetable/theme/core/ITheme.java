package com.zhuangfei.hputimetable.theme.core;

/**
 * Created by Liu ZhuangFei on 2019/1/11.
 */
public interface ITheme {
    ThemeModel onParse(String themeJson);
    void onLoad(ThemeModel themeModel);
}
