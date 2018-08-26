package com.zhuangfei.smartalert.listener;

import com.zhuangfei.smartalert.core.ChooseViewAlert;

public class OnChooseViewAdapter implements OnChooseViewListener{

	@Override
	public void onItemClick(int index) {
	}

	@Override
	public void onCancel(ChooseViewAlert chooseViewAlert) {
		chooseViewAlert.hide();
	}

}
