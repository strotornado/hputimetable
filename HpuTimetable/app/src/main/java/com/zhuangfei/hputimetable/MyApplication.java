package com.zhuangfei.hputimetable;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.litepal.LitePal;

/**
 * Created by Liu ZhuangFei on 2018/3/7.
 */

public class MyApplication extends TinkerApplication{
    private static MyApplication instance;

    public MyApplication(){
        super(ShareConstants.TINKER_ENABLE_ALL, "com.zhuangfei.hputimetable.MyApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        LitePal.initialize(this);
        ZXingLibrary.initDisplayOpinion(this);
//        LeakCanary.install(this);
    }

    public static MyApplication getInstance(){
        return instance;
    }

}
