package com.zhuangfei.hputimetable.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.toolkit.tools.ActivityTools;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ActivityTools.toActivity(SplashActivity.this,MainActivity.class);
            }
        },500);
    }
}
