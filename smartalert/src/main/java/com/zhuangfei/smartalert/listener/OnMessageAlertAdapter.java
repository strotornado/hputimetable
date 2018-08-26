package com.zhuangfei.smartalert.listener;

import com.zhuangfei.smartalert.core.MessageAlert;

import android.app.AlertDialog;

public class OnMessageAlertAdapter implements OnMessageAlertListener{
	
	@Override
	public void onConfirm(MessageAlert messageAlert) {
		messageAlert.hide();
	}

	@Override
	public void onCancel(MessageAlert messageAlert) {
		messageAlert.hide();
	}

}
