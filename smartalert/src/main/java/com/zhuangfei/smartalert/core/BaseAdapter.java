package com.zhuangfei.smartalert.core;

import android.app.AlertDialog;
import android.content.Context;

public class BaseAdapter<T> implements BaseAlert<T>{

	private Context context;
	
	private AlertDialog alertDialog;
	
	public BaseAdapter(Context context) {
		this.context=context;
	}
	
	protected void setAlertDialog(AlertDialog alertDialog) {
		this.alertDialog = alertDialog;
	}
	
	public AlertDialog getAlertDialog() {
		return alertDialog;
	}
	
	protected void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}
	
	@Override
	public T create() {
		return null;
	}

	@Override
	public void hide() {
		if(alertDialog!=null) alertDialog.hide();
	}

	@Override
	public void show() {
		if(alertDialog!=null) alertDialog.show();
	}

	@Override
	public void dimiss() {
		if(alertDialog!=null) alertDialog.dismiss();
	}
	
}
