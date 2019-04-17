package com.zhuangfei.hputimetable.listener;

/**
 * Created by Liu ZhuangFei on 2019/4/17.
 */
public interface IVipVerifyListener {
    void verifyPass();
    void needVerifyOrder();
    void verifyError();
}
