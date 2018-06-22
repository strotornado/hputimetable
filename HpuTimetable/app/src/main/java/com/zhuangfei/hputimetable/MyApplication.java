package com.zhuangfei.hputimetable;

import android.annotation.TargetApi;
import android.app.Application;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import org.litepal.LitePal;

import java.util.Locale;

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
    }

    public static MyApplication getInstance(){
        return instance;
    }

}
