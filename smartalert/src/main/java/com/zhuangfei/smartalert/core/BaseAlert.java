package com.zhuangfei.smartalert.core;

public interface BaseAlert<T>{

	public T create();
	public void hide();
	public void show();
	public void dimiss();
}
