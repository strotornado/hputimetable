package com.zhuangfei.hputimetable;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * Created by Liu ZhuangFei on 2018/3/7.
 */

public class MyApplication extends TinkerApplication{
    public MyApplication(){
        super(ShareConstants.TINKER_ENABLE_ALL, "com.zhuangfei.hputimetable.MyApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }
}
