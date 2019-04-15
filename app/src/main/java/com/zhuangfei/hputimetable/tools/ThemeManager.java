package com.zhuangfei.hputimetable.tools;

import android.content.Context;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.tools.ShareTools;

/**
 * Created by Liu ZhuangFei on 2019/4/14.
 */
public class ThemeManager {
    public static void apply(Context context) {
        int theme = ShareTools.getInt(context, ShareConstants.INT_THEME, 2);
        switch (theme) {
            case 0:
                context.setTheme(R.style.redTheme);
                break;
            case 1:
                context.setTheme(R.style.blueTheme);
                break;
            case 2:
                context.setTheme(R.style.blackTheme);
                break;
            default:
                context.setTheme(R.style.redTheme);
                break;

        }
    }
}
