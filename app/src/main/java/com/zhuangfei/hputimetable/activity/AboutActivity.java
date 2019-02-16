package com.zhuangfei.hputimetable.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.tv_version)
    TextView versionText;

    @BindView(R.id.id_pay_layout)
    LinearLayout payLayout;

    @BindView(R.id.id_content)
    LinearLayout contentLayout;

    @BindView(R.id.id_ali_pay)
    ImageView alipayView;

    @BindView(R.id.id_wx_pay)
    ImageView wxpayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        versionText.setText("版本号:"+VersionTools.getVersionName());

        String ali="http://www.liuzhuangfei.com/apis/area/images/alipay.jpg";
        String wx="http://www.liuzhuangfei.com/apis/area/images/wxpay.jpg";
        Glide.with(this).load(ali)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(alipayView);

        Glide.with(this).load(wx)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(wxpayView);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }

    @OnClick(R.id.id_zanzhu)
    public void onZanzhuClicked(){
        if(payLayout.getVisibility()== View.GONE){
            payLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        }else {
            payLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }
}
