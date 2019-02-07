package com.zhuangfei.hputimetable.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.tv_version)
    TextView versionText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        versionText.setText("版本号:"+VersionTools.getVersionName());
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }
}
