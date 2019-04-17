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
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.Md5Tools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        String name=getResources().getString(R.string.vip_name);
        String body=getResources().getString(R.string.vip_body);
        Integer amount=Integer.parseInt(getResources().getString(R.string.vip_money));
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String userId= DeviceTools.getDeviceId(this);
        String orderid=PayTools.getOrderId(userId);
        if(TextUtils.isEmpty(userId)){
            ToastTools.show(this,"请先开启读取设备IMEI权限");
            return;
        }
        PayTools.callPay(this, name, body, amount, orderid, userId, null, new PayResultListener() {
            @Override
            public void onFinish(Context context, Long payId, String orderId, String payUserId, EPayResult payResult, int payType, Integer amount) {
                if(payResult.getCode()==EPayResult.SUCCESS_CODE.getCode()){
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, 4);
                    Date date = cal.getTime();

                    PayLicense license=VipTools.getLicense(VipActivity.this,payId,date);
                    VipTools.registerVip(license);
                    ToastTools.show(VipActivity.this,getResources().getString(R.string.vip_success));
                }else{
                    ToastTools.show(VipActivity.this,getResources().getString(R.string.vip_error));
                }
                VipActivity.this.finish();
            }
        });
    }

    @OnClick(R.id.id_vip_btn2)
    public void onVipBtnClicked2(){
        String name=getResources().getString(R.string.vip_name2);
        String body=getResources().getString(R.string.vip_body2);
        Integer amount=Integer.parseInt(getResources().getString(R.string.vip_money2));
        String userId= DeviceTools.getDeviceId(this);
        String orderid=PayTools.getOrderId(userId);
        if(TextUtils.isEmpty(userId)){
            ToastTools.show(this,"请先开启读取设备IMEI权限");
            return;
        }
        PayTools.callPay(this, name, body, amount, orderid, userId, null, new PayResultListener() {
            @Override
            public void onFinish(Context context, Long payId, String orderId, String payUserId, EPayResult payResult, int payType, Integer amount) {
                if(payResult.getCode()==EPayResult.SUCCESS_CODE.getCode()){
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, 30);
                    Date date = cal.getTime();
                    PayLicense license=VipTools.getLicense(VipActivity.this,payId,date);
                    VipTools.registerVip(license);
                    ToastTools.show(VipActivity.this,getResources().getString(R.string.vip_success));
                }else{
                    ToastTools.show(VipActivity.this,getResources().getString(R.string.vip_error));
                }
                VipActivity.this.finish();
            }
        });
    }
}
