package com.zhuangfei.smartalert.core;

import com.zhuangfei.smartalert.R;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.smartalert.listener.OnMessageAlertListener;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageAlert extends BaseAdapter<MessageAlert> implements BaseAlert<MessageAlert> {

	private String title="提示";
	private String content="";
	private boolean isCancelEnable=false;
	private OnMessageAlertListener onMessageAlertListener;
	
	private TextView titleTextView=null;
	private TextView contentTextView=null;
	private LinearLayout cancelTextView=null;
	boolean hideButton=false;
	
	public MessageAlert(Context context) {
		super(context);
	}

	public MessageAlert(Context context,boolean hideButton) {
		super(context);
		this.hideButton=hideButton;
	}

	public MessageAlert setContent(String content) {
		this.content = content;
		if(contentTextView!=null){
			contentTextView.setText(content);
		}
		return this;
	}
	
	public MessageAlert setTitle(String title) {
		this.title = title;
		if(titleTextView!=null){
			titleTextView.setText(title);
		}
		return this;
	}
	
	public MessageAlert setCancelEnable(boolean isCancelEnable) {
		this.isCancelEnable = isCancelEnable;
		if(isCancelEnable&&cancelTextView!=null){
			cancelTextView.setVisibility(View.VISIBLE);
		}
		return this;
	}
	
	public MessageAlert setOnMessageAlertListener(OnMessageAlertListener onMessageAlertListener) {
		this.onMessageAlertListener = onMessageAlertListener;
		return this;
	}
	
	public MessageAlert create() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
		View view=LayoutInflater.from(getContext()).inflate(R.layout.alert_simplemassage_layout,null, false);
		//获取控件
		LinearLayout confirm=(LinearLayout) view.findViewById(R.id.id_simplealert_confirm);
		LinearLayout cancel=(LinearLayout) view.findViewById(R.id.id_simplealert_cancel);
		LinearLayout buttonLayout=(LinearLayout) view.findViewById(R.id.id_simplealert_buttoncontainer);
		TextView titleTextView=(TextView) view.findViewById(R.id.id_simplealert_title);
		final TextView contentTextView=(TextView) view.findViewById(R.id.id_simplealert_content);
		titleTextView.setText(title);
		contentTextView.setText(content);

		if(hideButton){
			buttonLayout.setVisibility(View.GONE);
		}
		
		builder.setView(view);
		
		this.titleTextView=titleTextView;
		this.contentTextView=contentTextView;
		this.cancelTextView=cancel;
		
		final AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		//设置事件监听
		if(onMessageAlertListener==null) onMessageAlertListener=new OnMessageAlertAdapter();
		
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onMessageAlertListener.onConfirm(MessageAlert.this);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onMessageAlertListener.onCancel(MessageAlert.this);
			}
		});
		if(isCancelEnable) cancel.setVisibility(View.VISIBLE);
		setAlertDialog(alertDialog);
		return this;
	};
}
