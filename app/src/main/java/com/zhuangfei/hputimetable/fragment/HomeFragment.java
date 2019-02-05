package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.ViewTools;

import butterknife.ButterKnife;

/**
 * @author Administrator 刘壮飞
 * 
 */
@SuppressLint({ "NewApi", "ValidFragment" })
public class HomeFragment extends LazyLoadFragment{

	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_home, container, false);
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
