package com.zhuangfei.smartalert.listener;

import com.zhuangfei.smartalert.core.ImageAlert;

public interface OnImageAlertListener {
	
	public void onConfirm(ImageAlert imageAlert);
	public void onCancel(ImageAlert imageAlert);
}
