package com.zhuangfei.hputimetable.tools;

public interface ISecurity {
    public String encrypt(String key, String value);

    public String decrypt(String key, String encrypted);
}