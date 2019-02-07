package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhuangfei.hputimetable.R;

import butterknife.ButterKnife;

/**
 * @author Administrator 刘壮飞
 * 
 */
@SuppressLint({ "NewApi", "ValidFragment" })
public class ServiceStationFragment extends LazyLoadFragment{

	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_service_station, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this,view);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	protected void lazyLoad() {
	}
}
