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
            case 3:
                context.setTheme(R.style.ziTheme);
                break;
            case 4:
                context.setTheme(R.style.zongTheme);
                break;
            case 5:
                context.setTheme(R.style.qingTheme);
                break;
            case 6:
                context.setTheme(R.style.lanTheme);
                break;
            case 7:
                context.setTheme(R.style.chengTheme);
                break;
            case 8:
                context.setTheme(R.style.grayTheme);
                break;
            case 9:
                context.setTheme(R.style.coolapkTheme);
                break;
            default:
                context.setTheme(R.style.blackTheme);
                break;

        }
    }
}
