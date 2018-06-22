package com.zhuangfei.smartalert.core;

import java.util.ArrayList;
import java.util.List;

import com.zhuangfei.smartalert.R;
import com.zhuangfei.smartalert.R.id;
import com.zhuangfei.smartalert.listener.OnChooseViewAdapter;
import com.zhuangfei.smartalert.listener.OnChooseViewListener;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.smartalert.listener.OnMessageAlertListener;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChooseViewAlert extends BaseAdapter<ChooseViewAlert> implements BaseAlert<ChooseViewAlert> {

	private String title="提示";
	private OnChooseViewListener onChooseViewListener;
	
	private TextView titleTextView=null;
	private int count=0;
	private List<String> itemTexts=new ArrayList<>();
	
	public ChooseViewAlert(Context context) {
		super(context);
	}

	public ChooseViewAlert setTitle(String title) {
		this.title = title;
		if(titleTextView!=null){
			titleTextView.setText(title);
		}
		return this;
	}
	
	public ChooseViewAlert setOnChooseViewListener(OnChooseViewListener onChooseViewListener) {
		this.onChooseViewListener = onChooseViewListener;
		return this;
	}
	
	public ChooseViewAlert setItemTexts(List<String> list) {
		this.itemTexts = list;
		this.count=itemTexts.size();
		return this;
	}
	
	public ChooseViewAlert create() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
		View view=LayoutInflater.from(getContext()).inflate(R.layout.alert_chooseview_layout,null, false);
		//获取控件
		LinearLayout container=(LinearLayout) view.findViewById(R.id.id_container);
		LinearLayout cancel=(LinearLayout) view.findViewById(R.id.id_alert_cancel);
		TextView titleTextView=(TextView) view.findViewById(R.id.id_simplealert_title);
		titleTextView.setText(title);
		builder.setView(view);
		
		addView(container);
		
		this.titleTextView=titleTextView;
		
		final AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		//设置事件监听
		if(onChooseViewListener==null) onChooseViewListener=new OnChooseViewAdapter();
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onChooseViewListener.onCancel(ChooseViewAlert.this);
			}
		});
		
		setAlertDialog(alertDialog);
		return this;
	}

	private void addView(LinearLayout container) {
		if(container==null||count<=0||count>=30||itemTexts==null) return;
		container.removeAllViews();
		for(int i=0;i<count;i++){
			final int tmp=i;
			View view=LayoutInflater.from(getContext()).inflate(R.layout.item_chooseview_layout, null);
			LinearLayout layout=(LinearLayout) view.findViewById(R.id.id_item_layout);
			TextView textView=(TextView) view.findViewById(R.id.id_item_textview);
			textView.setText(itemTexts.get(i));
			layout.setBackgroundColor(getColor(i));
			layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					onChooseViewListener.onItemClick(tmp);
				}
			});
			container.addView(view);
		}
	};
	
	private int getColor(int i){
		int color=getContext().getResources().getColor(R.color.color_1);
		switch (i) {
		case 0:
			color=getContext().getResources().getColor(R.color.color_1);
			break;
		case 1:
			color=getContext().getResources().getColor(R.color.color_2);
			break;
		case 2:
			color=getContext().getResources().getColor(R.color.color_3);
			break;
		case 3:
			color=getContext().getResources().getColor(R.color.color_4);
			break;
		case 4:
			color=getContext().getResources().getColor(R.color.color_5);
			break;
		case 5:
			color=getContext().getResources().getColor(R.color.color_6);
			break;
		case 6:
			color=getContext().getResources().getColor(R.color.color_7);
			break;
		case 7:
			color=getContext().getResources().getColor(R.color.color_8);
			break;
		case 8:
			color=getContext().getResources().getColor(R.color.color_9);
			break;
		case 9:
			color=getContext().getResources().getColor(R.color.color_10);
			break;
		case 10:
			color=getContext().getResources().getColor(R.color.color_11);
			break;

		default:
			break;
		}
		return color;
	}
}
