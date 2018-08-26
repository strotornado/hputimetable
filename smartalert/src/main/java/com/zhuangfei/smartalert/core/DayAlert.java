package com.zhuangfei.smartalert.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhuangfei.smartalert.R;
import com.zhuangfei.smartalert.listener.OnDayAlertAdapter;
import com.zhuangfei.smartalert.listener.OnDayAlertListener;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.smartalert.listener.OnMessageAlertListener;
import com.zhuangfei.smartalert.listener.OnWeekAlertAdapter;
import com.zhuangfei.smartalert.listener.OnWeekAlertListener;

import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DayAlert extends BaseAdapter<DayAlert> implements BaseAlert<DayAlert>{

	private String title="提示";
	private OnDayAlertListener onDayAlertListener;
	
	private TextView titleTextView=null;
	private List<String> result=new ArrayList<>();
	
	private ListView listView;
	private SimpleAdapter simpleAdapter;
	private String[] arr={"周一","周二","周三","周四","周五","周六","周日"};
	private List<Map<String, String>> items=new ArrayList<>();
	private boolean isShown=false;
	
	public boolean isShown() {
		return isShown;
	}
	
	public DayAlert(Context context) {
		super(context);
	}
	
	public DayAlert setTitle(String title) {
		this.title = title;
		if(titleTextView!=null){
			titleTextView.setText(title);
		}
		return this;
	}
	
	public DayAlert setOnDayAlertListener(OnDayAlertListener onDayAlertListener) {
		this.onDayAlertListener = onDayAlertListener;
		return this;
	}
	
	public DayAlert create() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
		View view=LayoutInflater.from(getContext()).inflate(R.layout.alert_day_layout,null, false);
		//获取控件
		LinearLayout cancel=(LinearLayout) view.findViewById(R.id.id_simplealert_cancel);
		TextView titleTextView=(TextView) view.findViewById(R.id.id_simplealert_title);
		titleTextView.setText(title);
		builder.setView(view);
		
		this.titleTextView=titleTextView;
		
		initView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		//设置事件监听
		if(onDayAlertListener==null) onDayAlertListener=new OnDayAlertAdapter();
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onDayAlertListener.onCancel(DayAlert.this);
			}
		});
		setAlertDialog(alertDialog);
		isShown=true;
		return this;
	}

	private void initView(View view) {
		for(int i=0;i<arr.length;i++){
			Map<String, String> map=new HashMap<>();
			map.put("value",arr[i]);
			items.add(map);
		}
		listView=(ListView) view.findViewById(R.id.id_listView);
		simpleAdapter=new SimpleAdapter(getContext(),items, R.layout.item_day_layout,
				new String[]{"value"}, new int[]{R.id.id_item_textview});
		listView.setAdapter(simpleAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onDayAlertListener.onConfirm(DayAlert.this,arg2);
			}
			
		});
	}

}
