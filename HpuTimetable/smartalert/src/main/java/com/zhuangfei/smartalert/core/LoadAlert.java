package com.zhuangfei.smartalert.core;

import com.zhuangfei.smartalert.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class LoadAlert extends BaseAdapter<LoadAlert> implements BaseAlert<LoadAlert>{
	
	public LoadAlert(Context context) {
		super(context);
	}
	
	private String message="加载中...";
			
	public LoadAlert setMessage(String message) {
		this.message = message;
		return this;
	}
	
	@Override
	public LoadAlert create() {
		Context context=getContext();
		AlertDialog.Builder builder=new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_DARK);
		View view=LayoutInflater.from(context).inflate(R.layout.alert_load_layout,null, false);
		//获取控件
		TextView textView=(TextView) view.findViewById(R.id.id_loaddata_text);
		textView.setText(message);
		//设置view
		builder.setView(view);
		AlertDialog alertDialog=builder.create();
		
		alertDialog.setCanceledOnTouchOutside(false);
		setAlertDialog(alertDialog);
		return this;
	}
	
	@Override
	public void show() {
		super.show();
		Window dialogWindow = getAlertDialog().getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();

		lp.width=OtherUtils.dip2px(getContext(), 160);
		lp.height=OtherUtils.dip2px(getContext(), 160);
		//设置
		dialogWindow.setAttributes(lp);
	}
}
