package com.zhuangfei.smartalert.listener;

import com.zhuangfei.smartalert.core.ChooseViewAlert;

public interface OnChooseViewListener {
	
	public void onItemClick(int index);
	
	public void onCancel(ChooseViewAlert chooseViewAlert);
}
