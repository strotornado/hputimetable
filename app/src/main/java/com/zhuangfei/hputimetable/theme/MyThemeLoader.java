package com.zhuangfei.hputimetable.theme;

import android.text.TextUtils;

import com.zhuangfei.hputimetable.theme.core.ThemeLoader;
import com.zhuangfei.hputimetable.theme.core.ThemeModel;
import com.zhuangfei.timetable.TimetableView;
/**
 * Created by Liu ZhuangFei on 2019/1/11.
 */
public class MyThemeLoader extends ThemeLoader {

    TimetableView timetableView;

    public MyThemeLoader(TimetableView view) {
        timetableView = view;
    }

    @Override
    public void onLoad(ThemeModel themeModel) {
        super.onLoad(themeModel);
        if (timetableView == null||themeModel==null) return;
        if(TextUtils.isEmpty(themeModel.getOperator())){
            themeModel.setOperator(OPERATOR_DEFAULT);
        }
        switch (themeModel.getOperator()) {
            case OPERATOR_WAKEUP:
                break;
            default:
                break;
        }
    }
}
