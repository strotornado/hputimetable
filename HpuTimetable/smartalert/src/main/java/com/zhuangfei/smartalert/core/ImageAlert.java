package com.zhuangfei.smartalert.core;

import com.zhuangfei.smartalert.R;
import com.zhuangfei.smartalert.listener.OnImageAlertListener;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.smartalert.listener.OnMessageAlertListener;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageAlert extends BaseAdapter<ImageAlert> implements BaseAlert<ImageAlert> {

	private String title="提示";
	private String content="";
	private OnImageAlertListener onImageAlertListener;
	
	private TextView titleTextView=null;
	private ImageView imageView=null;
	private LinearLayout cancelTextView=null;
	
	private Bitmap bitmap=null;
	
	public ImageAlert(Context context) {
		super(context);
	}

	public ImageAlert setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		if(imageView!=null) imageView.setImageBitmap(bitmap);
		return this;
	}
	
	public ImageAlert setTitle(String title) {
		this.title = title;
		if(titleTextView!=null){
			titleTextView.setText(title);
		}
		return this;
	}
	
	public ImageAlert setOnImageAlertListener(OnImageAlertListener onImageAlertListener) {
		this.onImageAlertListener = onImageAlertListener;
		return this;
	}
	
	public ImageAlert create() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
		View view=LayoutInflater.from(getContext()).inflate(R.layout.alert_image_layout,null, false);
		//获取控件
		LinearLayout confirm=(LinearLayout) view.findViewById(R.id.id_simplealert_confirm);
		LinearLayout cancel=(LinearLayout) view.findViewById(R.id.id_simplealert_cancel);
		TextView titleTextView=(TextView) view.findViewById(R.id.id_simplealert_title);
		ImageView imageView=(ImageView) view.findViewById(R.id.id_imageview);
		
		titleTextView.setText(title);
		if(bitmap!=null) imageView.setImageBitmap(bitmap);
		builder.setView(view);
		
		this.titleTextView=titleTextView;
		this.imageView=imageView;
		this.cancelTextView=cancel;
		
		final AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		//设置事件监听
		if(onImageAlertListener==null) onImageAlertListener=new OnImageAlertListener() {
			
			@Override
			public void onConfirm(ImageAlert imageAlert) {
			}
			
			@Override
			public void onCancel(ImageAlert imageAlert) {
			}
		};
		
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onImageAlertListener.onConfirm(ImageAlert.this);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onImageAlertListener.onCancel(ImageAlert.this);
			}
		});
		setAlertDialog(alertDialog);
		return this;
	};
}
