package com.zhuangfei.hputimetable.theme;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;

import com.zhuangfei.hputimetable.fragment.ScheduleFragment;
import com.zhuangfei.hputimetable.theme.core.ThemeLoader;
import com.zhuangfei.hputimetable.theme.core.ThemeModel;
import com.zhuangfei.hputimetable.theme.wakeup.OnWakeupDateBuildAdapter;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.model.ScheduleColorPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2019/1/11.
 */
public class MyThemeLoader extends ThemeLoader {

    TimetableView timetableView;
    IThemeView themeView;

    public MyThemeLoader(IThemeView view) {
        themeView = view;
        timetableView=themeView.getTimetableView();
    }

    @Override
    public void onSuccess(ThemeModel themeModel) {
        super.onSuccess(themeModel);
        if (timetableView == null||themeModel==null) return;
        if(TextUtils.isEmpty(themeModel.getOperator())){
            themeModel.setOperator(OPERATOR_DEFAULT);
        }
        parseThemeForAll(themeModel);
        switch (themeModel.getOperator()) {
            case OPERATOR_WAKEUP:
                timetableView.alpha(0f,0f,0.6f)
                    .callback(new OnWakeupDateBuildAdapter());
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(String exception) {
        super.onError(exception);
    }

    /**
     * 解析一些通用的课表主题设置
     * @param themeModel
     */
    private void parseThemeForAll(ThemeModel themeModel) {
        int themeVersion=themeModel.getVersion();
        int supportVersion=themeModel.getMinSupportVersion();

        List<String> colors=themeModel.getItemColors();
        String colorsMode=themeModel.getItemColorsMode();
        ScheduleColorPool colorPool=timetableView.colorPool();
        List<Integer> colorList=new ArrayList<>();
        for(String color:colors){
            colorList.add(Color.parseColor(color));
        }
        if(colorsMode.equals(MODE_APPEND)){
            colorPool.addAll(colorList);
        }
        if(colorsMode.equals(MODE_COVER)){
            colorPool.clear().addAll(colorList);
        }

        Integer userlessColor=Color.parseColor(themeModel.getUselessColor());
        colorPool.setUselessColor(userlessColor);

        float dateAlpha=(float)themeModel.getDateAlpha();
        float sideAlpha=(float)themeModel.getSideAlpha();
        float itemAlpha=(float)themeModel.getItemAlpha();

        timetableView.alpha(dateAlpha,sideAlpha,itemAlpha)
                .isShowNotCurWeek(themeModel.isShowNotCurWeek())
                .isShowWeekends(themeModel.isShowWeekends())
                .isShowFlaglayout(themeModel.isShowFlaglayout())
                .maxSlideItem(themeModel.getMaxSideItem())
                .monthWidthDp(themeModel.getSideWidth())
                .marTop(themeModel.getMarTop())
                .marLeft(themeModel.getMarLeft())
                .itemHeight(themeModel.getItemHeight())
                .cornerAll(themeModel.getItemCorner());
    }

    @Override
    public void execute() {
        super.execute();
    }
}
