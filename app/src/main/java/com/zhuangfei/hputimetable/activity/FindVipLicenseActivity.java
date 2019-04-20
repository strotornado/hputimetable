package com.zhuangfei.hputimetable.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.payelves.sdk.EPay;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.listener.QueryOrderListener;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.listener.VipVerifyResult;
import com.zhuangfei.hputimetable.model.PayLicense;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.PayTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.hputimetable.tools.VipTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FindVipLicenseActivity extends AppCompatActivity {

    @BindView(R.id.id_vip_order)
    EditText orderNumberEdit;

    @BindView(R.id.id_device_info)
    TextView deviceInfoText;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_vip_license);
        ButterKnife.bind(this);
        context=this;
        String userId= DeviceTools.getDeviceId(this);
        if(TextUtils.isEmpty(userId)){
            ToastTools.show(this,"请先开启读取设备IMEI权限");
            finish();
        }
        deviceInfoText.setText("当前设备:"+userId);
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }

    @OnClick(R.id.id_find_vip)
    public void onFindVipButtonClicked(){
        String userId= DeviceTools.getDeviceId(this);
        if(TextUtils.isEmpty(userId)){
            ToastTools.show(this,"请先开启读取设备IMEI权限");
            return;
        }
        String orderNumber=orderNumberEdit.getText().toString();
        long orderLong=0;
        try {
            orderLong=Long.parseLong(orderNumber);
        }catch (Exception e){
            ToastTools.show(this,"Exception:"+e.getMessage());
            orderLong=0;
        }
        if(TextUtils.isEmpty(orderNumber)){
            ToastTools.show(this,"订单号不可以为空");
            return;
        }
        if(orderLong==0){
            ToastTools.show(this,"订单解析为Long类型时出错");
            return;
        }
        PayTools.checkPaySdkInit(this);
        final long finalOrderLong = orderLong;
        EPay.getInstance(this).queryOrder(orderLong, new QueryOrderListener() {
            @Override
            public void onFinish(boolean isSuccess, String msg, QueryOrderModel model) {
                if(!isSuccess||model==null){
                    if(!TextUtils.isEmpty(msg)){
                        ToastTools.show(getContext(),msg);
                    }
                    return;
                }
                if(model.getPayStatus()!=null&&model.getPayStatus().equals("SUCCESS")){
                    String payTime=model.getPayTime();
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date create=sdf.parse(payTime);
                        Integer amount=PayTools.getMoneyByOrderId(model.getOrderId());
                        PayLicense license=VipTools.getLicense(getContext(), finalOrderLong,create,amount);
                        VipVerifyResult verifyResult=VipTools.isVip(getContext(),license);
                        if(verifyResult.isSuccess()||verifyResult.isNeedVerify()){
                            VipTools.registerVip(license);
                            ToastTools.show(getContext(),"高级版证书恢复成功，等待验证..");
                            finish();
                        }else{
                            ToastTools.show(getContext(),"验证失败:"+verifyResult.getMsg());
                        }
                    } catch (ParseException e) {
                        ToastTools.show(getContext(),"Exception:"+e.getMessage());
                        e.printStackTrace();
                    }
                }else{
                    ToastTools.show(getContext(),"订单未支付或者状态码为Null");
                }
            }
        });
    }

    @OnClick(R.id.id_howtofind)
    public void onLookOrderButtonClicked(){
        BundleModel model=new BundleModel();
        model.put("url","http://www.liuzhuangfei.com/apis/area/public/findorder.html");
        model.put("title","如何查看商家订单号");
        model.setFromClass(FindVipLicenseActivity.class);
        ActivityTools.toActivityWithout(this, WebViewActivity.class,model);
    }
}
