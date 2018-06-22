package com.zhuangfei.smartalert.core;

import java.util.ArrayList;
import java.util.List;

import com.zhuangfei.smartalert.R;
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
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeekAlert extends BaseAdapter<WeekAlert> implements BaseAlert<WeekAlert>,OnTouchListener{

	private String title="提示";
	private boolean isCancelEnable=false;
	private OnWeekAlertListener onWeekAlertListener;
	
	private TextView titleTextView=null;
	private List<String> result=new ArrayList<>();
	
	private TextView weekLayout1;
	private TextView weekLayout2;
	private TextView weekLayout3;
	private TextView weekLayout4;
	private TextView weekLayout5;
	
	private TextView weekLayout6;
	private TextView weekLayout7;
	private TextView weekLayout8;
	private TextView weekLayout9;
	private TextView weekLayout10;
	
	private TextView weekLayout11;
	private TextView weekLayout12;
	private TextView weekLayout13;
	private TextView weekLayout14;
	private TextView weekLayout15;
	
	private TextView weekLayout16;
	private TextView weekLayout17;
	private TextView weekLayout18;
	private TextView weekLayout19;
	private TextView weekLayout20;
	
	private List<TextView> weekList=new ArrayList<>();
	private boolean isShown=false;
	
	public boolean isShown() {
		return isShown;
	}
	
	public WeekAlert(Context context) {
		super(context);
	}
	
	public WeekAlert setTitle(String title) {
		this.title = title;
		if(titleTextView!=null){
			titleTextView.setText(title);
		}
		return this;
	}
	
	public WeekAlert setCancelEnable(boolean isCancelEnable) {
		this.isCancelEnable = isCancelEnable;
		return this;
	}
	
	public WeekAlert setOnWeekAlertListener(OnWeekAlertListener onWeekAlertListener) {
		this.onWeekAlertListener = onWeekAlertListener;
		return this;
	}
	
	public WeekAlert create() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
		View view=LayoutInflater.from(getContext()).inflate(R.layout.alert_week_layout,null, false);
		//获取控件
		LinearLayout confirm=(LinearLayout) view.findViewById(R.id.id_simplealert_confirm);
		LinearLayout cancel=(LinearLayout) view.findViewById(R.id.id_simplealert_cancel);
		TextView titleTextView=(TextView) view.findViewById(R.id.id_simplealert_title);
		titleTextView.setText(title);
		builder.setView(view);
		
		this.titleTextView=titleTextView;
		
		initView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		//设置事件监听
		if(onWeekAlertListener==null) onWeekAlertListener=new OnWeekAlertAdapter();
		
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				List<Integer> thisResult=new ArrayList<>();
				for(int i=0;i<result.size();i++){
					thisResult.add(Integer.parseInt(result.get(i)));
				}
				for(int i=0;i<thisResult.size()-1;i++){
					int minValue=thisResult.get(i);
					int minSite=i;
					for(int j=i+1;j<thisResult.size();j++){
						if(thisResult.get(j)<minValue){
							minValue=thisResult.get(j);
							minSite=j;
						}
					}
					int tmp=thisResult.get(i);
					thisResult.set(i, minValue);
					thisResult.set(minSite, tmp);
				}
				onWeekAlertListener.onConfirm(WeekAlert.this,thisResult);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onWeekAlertListener.onCancel(WeekAlert.this);
			}
		});
		if(isCancelEnable) cancel.setVisibility(View.VISIBLE);
		setAlertDialog(alertDialog);
		isShown=true;
		return this;
	}

	private void initView(View view) {
		weekLayout1=(TextView) view.findViewById(R.id.id_week_layout1);
		weekLayout2=(TextView) view.findViewById(R.id.id_week_layout2);
		weekLayout3=(TextView) view.findViewById(R.id.id_week_layout3);
		weekLayout4=(TextView) view.findViewById(R.id.id_week_layout4);
		weekLayout5=(TextView) view.findViewById(R.id.id_week_layout5);
		
		weekLayout6=(TextView) view.findViewById(R.id.id_week_layout6);
		weekLayout7=(TextView) view.findViewById(R.id.id_week_layout7);
		weekLayout8=(TextView) view.findViewById(R.id.id_week_layout8);
		weekLayout9=(TextView) view.findViewById(R.id.id_week_layout9);
		weekLayout10=(TextView) view.findViewById(R.id.id_week_layout10);
		
		weekLayout11=(TextView) view.findViewById(R.id.id_week_layout11);
		weekLayout12=(TextView) view.findViewById(R.id.id_week_layout12);
		weekLayout13=(TextView) view.findViewById(R.id.id_week_layout13);
		weekLayout14=(TextView) view.findViewById(R.id.id_week_layout14);
		weekLayout15=(TextView) view.findViewById(R.id.id_week_layout15);
		
		weekLayout16=(TextView) view.findViewById(R.id.id_week_layout16);
		weekLayout17=(TextView) view.findViewById(R.id.id_week_layout17);
		weekLayout18=(TextView) view.findViewById(R.id.id_week_layout18);
		weekLayout19=(TextView) view.findViewById(R.id.id_week_layout19);
		weekLayout20=(TextView) view.findViewById(R.id.id_week_layout20);
		
		weekList.add(weekLayout1);
		weekList.add(weekLayout2);
		weekList.add(weekLayout3);
		weekList.add(weekLayout4);
		weekList.add(weekLayout5);
		
		weekList.add(weekLayout6);
		weekList.add(weekLayout7);
		weekList.add(weekLayout8);
		weekList.add(weekLayout9);
		weekList.add(weekLayout10);
		
		weekList.add(weekLayout11);
		weekList.add(weekLayout12);
		weekList.add(weekLayout13);
		weekList.add(weekLayout14);
		weekList.add(weekLayout15);
		
		weekList.add(weekLayout16);
		weekList.add(weekLayout17);
		weekList.add(weekLayout18);
		weekList.add(weekLayout19);
		weekList.add(weekLayout20);
		
		for(int i=0;i<weekList.size();i++){
			weekList.get(i).setOnTouchListener(this);
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		int cur=-1;
		for(int i=0;i<weekList.size();i++){
			if(weekList.get(i)==arg0){
				cur=i+1;
				break;
			}
		}
		
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(result.indexOf(""+cur)!=-1){
				arg0.setBackgroundColor(Color.WHITE);
				result.remove(""+cur);
			}else {
				arg0.setBackgroundColor(getContext().getResources().getColor(R.color.app_gadblue));
				result.add(cur+"");
			}
			break;

		default:
			break;
		}
		return true;
	}

	private void initLayout() {
	}

	public WeekAlert setDefault(List<Integer> list){
		result.clear();
		for(int i=0;i<list.size();i++){
			int m=list.get(i);
			weekList.get(m-1).setBackgroundColor(getContext().getResources().getColor(R.color.app_gadblue));
			result.add(m+"");
		}
		return this;
	}
}
