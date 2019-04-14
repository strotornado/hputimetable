package com.zhuangfei.hputimetable.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.payelves.sdk.enums.EPayResult;
import com.payelves.sdk.listener.PayResultListener;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        finish();
    }

    @OnClick(R.id.id_vip_btn)
    public void onVipBtnClicked(){
        String name="怪兽课表高级版";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String userId= DeviceTools.getDeviceId(this);
        String orderid=sdf.format(new Date())+"_"+(int)(Math.random()*1000)+"_gs";
        if(!TextUtils.isEmpty(userId)){
            orderid=orderid+userId;
        }
        PayTools.callPay(this, name, "8.8元", 880, orderid, null, null, new PayResultListener() {
            @Override
            public void onFinish(Context context, Long payId, String orderId, String payUserId, EPayResult payResult, int payType, Integer amount) {
                if(payResult.getCode()==EPayResult.SUCCESS_CODE.getCode()){
                    VipTools.registerVip(VipActivity.this);
                    ToastTools.show(VipActivity.this,"支付成功，高级版已开启");
                }else{
                    ToastTools.show(VipActivity.this,"支付失败");
                }
                VipActivity.this.finish();
            }
        });
    }
}
