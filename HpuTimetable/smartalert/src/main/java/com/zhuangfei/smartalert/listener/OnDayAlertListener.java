package com.zhuangfei.smartalert.listener;

import java.util.List;

import com.zhuangfei.smartalert.core.DayAlert;
import com.zhuangfei.smartalert.core.MessageAlert;
import com.zhuangfei.smartalert.core.WeekAlert;

public interface OnDayAlertListener {
	
	public void onConfirm(DayAlert dayAlert, int result);
	public void onCancel(DayAlert dayAlert);
}
