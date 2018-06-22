package com.zhuangfei.smartalert.listener;

import java.util.List;

import com.zhuangfei.smartalert.core.MessageAlert;
import com.zhuangfei.smartalert.core.WeekAlert;

public interface OnWeekAlertListener {
	
	public void onConfirm(WeekAlert messageAlert, List<Integer> result);
	public void onCancel(WeekAlert messageAlert);
}
