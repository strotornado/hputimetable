package com.zhuangfei.smartalert.listener;

import com.zhuangfei.smartalert.core.MessageAlert;

public interface OnMessageAlertListener {
	
	public void onConfirm(MessageAlert messageAlert);
	public void onCancel(MessageAlert messageAlert);
}
