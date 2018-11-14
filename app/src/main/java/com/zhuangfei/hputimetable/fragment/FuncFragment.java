package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.AdapterSchoolActivity;
import com.zhuangfei.hputimetable.AdapterTipActivity;
import com.zhuangfei.hputimetable.AddTimetableActivity;
import com.zhuangfei.hputimetable.HpuRepertoryActivity;
import com.zhuangfei.hputimetable.ImportMajorActivity;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.MenuActivity;
import com.zhuangfei.hputimetable.MultiScheduleActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.ScanActivity;
import com.zhuangfei.hputimetable.SearchSchoolActivity;
import com.zhuangfei.hputimetable.TimetableDetailActivity;
import com.zhuangfei.hputimetable.UploadHtmlActivity;
import com.zhuangfei.hputimetable.WebViewActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Administrator 刘壮飞
 * 
 */
@SuppressLint({ "NewApi", "ValidFragment" })
public class FuncFragment extends LazyLoadFragment implements OnNoticeUpdateListener{

	private View mView;

	@BindView(R.id.id_cardview_layout)
	LinearLayout cardLayout;

	@BindView(R.id.id_cardview_today)
	TextView todayInfo;

	@BindView(R.id.id_cardview_layout2)
	LinearLayout cardLayout2;

	@BindView(R.id.id_cardview_today2)
	TextView todayInfo2;

	OnSwitchPagerListener onSwitchPagerListener;
	OnUpdateCourseListener onUpdateCourseListener;

	@BindView(R.id.id_display)
	TextView display;

	@BindView(R.id.id_func_schedulename)
	TextView scheduleNameText;

	@BindView(R.id.id_func_schedulename2)
	TextView scheduleNameText2;

	@BindView(R.id.id_img1)
	ImageView imageView1;

	@BindView(R.id.id_img2)
	ImageView imageView2;

	@BindView(R.id.id_img3)
	ImageView imageView3;

	@BindView(R.id.id_img4)
	ImageView imageView4;

	@BindView(R.id.tv_count)
	TextView countTextView;

	@BindView(R.id.tv_counttip)
	TextView countTipTextView;

	@BindView(R.id.cv_dayview)
	CardView dayView;

	@BindView(R.id.cv_dayview2)
	CardView dayView2;

	@BindView(R.id.cv_bind)
	CardView bindView;

	@BindView(R.id.iv_search)
	ImageView searchImageView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_func, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this,view);
		super.onViewCreated(view, savedInstanceState);
	}

	public void showBindView(){
		int v=ShareTools.getInt(getActivity(),"isAlone",0);
		if(v==0){
			bindView.setVisibility(View.VISIBLE);
		}else{
			bindView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void lazyLoad() {
		inits();
		getValue("1f088b55140a49e101e79c420b19bce6");
	}

	private void inits() {
		int color=getResources().getColor(R.color.colorPrimary);
		searchImageView.setColorFilter(Color.BLACK);
        display.setMovementMethod(LinkMovementMethod.getInstance());
		imageView1.setColorFilter(color);
		imageView2.setColorFilter(color);
		imageView3.setColorFilter(color);
		imageView4.setColorFilter(color);
		findData();
	}

	@OnClick(R.id.id_cardview2_cancel)
	public void cancelBind(){
		ShareTools.putInt(getActivity(),ShareConstants.INT_SCHEDULE_NAME_ID2,-1);
		dayView2.setVisibility(View.GONE);
		showBindView();
	}

	public void createCardView(List<Schedule> models, ScheduleName newName){
		cardLayout.removeAllViews();
		SimpleDateFormat sdf2=new SimpleDateFormat("EEEE");
		int curWeek = TimetableTools.getCurWeek(getActivity());
		String html="第<b>"+curWeek+"</b>周  "+sdf2.format(new Date());
		CharSequence charSequence;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			charSequence = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
		} else {
			charSequence = Html.fromHtml(html);
		}

		todayInfo.setText(charSequence);

		if(newName!=null){
			scheduleNameText.setText(newName.getName());
		}

		LayoutInflater inflater=LayoutInflater.from(getActivity());
		if(models==null){
			countTextView.setText("0");
			countTipTextView.setText("你还没有添加数据呀!");
			dayView.setVisibility(View.GONE);

			View view=inflater.inflate(R.layout.item_empty,null ,false);
			TextView infoText=view.findViewById(R.id.item_empty);
			view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(onSwitchPagerListener!=null){
						onSwitchPagerListener.onPagerSwitch();
					}
				}
			});
			infoText.setText("本地没有数据,去添加!");
			cardLayout.addView(view);
		}else if(models.size()==0){
			countTextView.setText("0");
			countTipTextView.setText("今天没有课，好闲呀~");
			dayView.setVisibility(View.GONE);
//			View view=inflater.inflate(R.layout.item_empty,null ,false);
//			TextView infoText=view.findViewById(R.id.item_empty);
//			view.findViewById(R.id.item_empty).setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View view) {
//					if(onSwitchPagerListener!=null){
//						onSwitchPagerListener.onPagerSwitch();
//					}
//				}
//			});
//			cardLayout.addView(view);

		}else{
			countTextView.setText(""+models.size());
			if(models.size()<3) countTipTextView.setText("今天课程较少，非常轻松~");
			else if(models.size()<5) countTipTextView.setText("今天课程适中，加油~");
			else countTipTextView.setText("好多的课呀，有点慌~");
			dayView.setVisibility(View.VISIBLE);

			for(int i=0;i<models.size();i++){
				final Schedule schedule=models.get(i);
				if(schedule==null)continue;
				View view=inflater.inflate(R.layout.item_cardview,null ,false);
				TextView startText=view.findViewById(R.id.id_item_start);
				TextView nameText=view.findViewById(R.id.id_item_name);
				TextView roomText=view.findViewById(R.id.id_item_room);
				String name=schedule.getName();
				String room=schedule.getRoom();
				if(TextUtils.isEmpty(name)) name="课程名未知";
				if(TextUtils.isEmpty(room)) room="上课地点未知";

				nameText.setText(name);
				roomText.setText(room);
				startText.setText(schedule.getStart() + " - " + (schedule.getStart() + schedule.getStep() - 1));
				view.findViewById(R.id.id_item_clicklayout).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						List<Schedule> list=new ArrayList<>();
						list.add(schedule);
						BundleModel model = new BundleModel();
						model.put("timetable", list);
						model.setFromClass(getActivity().getClass());
						model.put("item",0);
						ActivityTools.toActivityWithout(getActivity(), TimetableDetailActivity.class, model);
					}
				});
				cardLayout.addView(view);
			}
		}
	}

	public void createCardView2(List<Schedule> models, ScheduleName newName){
		cardLayout2.removeAllViews();
		dayView2.setVisibility(View.VISIBLE);
		SimpleDateFormat sdf2=new SimpleDateFormat("EEEE");
		int curWeek = TimetableTools.getCurWeek(getActivity());
		String html="Ta的课表";
		CharSequence charSequence;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			charSequence = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
		} else {
			charSequence = Html.fromHtml(html);
		}

		todayInfo2.setText(charSequence);
		todayInfo2.setTextColor(getResources().getColor(R.color.app_red_orange));

		if(newName!=null){
			scheduleNameText2.setText(newName.getName());
		}

		LayoutInflater inflater=LayoutInflater.from(getActivity());
		if(models==null){
			View view=inflater.inflate(R.layout.item_empty,null ,false);
			TextView infoText=view.findViewById(R.id.item_empty);
			infoText.setText("本地没有数据,去添加!");
			cardLayout2.addView(view);
		}else if(models.size()==0){
			View view=inflater.inflate(R.layout.item_empty,null ,false);
			TextView infoText=view.findViewById(R.id.item_empty);
			infoText.setText("Ta今天没有课");
			cardLayout2.addView(view);
		}else{
			for(int i=0;i<models.size();i++){
				final Schedule schedule=models.get(i);
				if(schedule==null)continue;
				View view=inflater.inflate(R.layout.item_cardview,null ,false);
				TextView startText=view.findViewById(R.id.id_item_start);
				TextView nameText=view.findViewById(R.id.id_item_name);
				TextView roomText=view.findViewById(R.id.id_item_room);
				String name=schedule.getName();
				String room=schedule.getRoom();
				if(TextUtils.isEmpty(name)) name="课程名未知";
				if(TextUtils.isEmpty(room)) room="上课地点未知";

				nameText.setText(name);
				roomText.setText(room);
				startText.setText(schedule.getStart() + " - " + (schedule.getStart() + schedule.getStep() - 1));
				view.findViewById(R.id.id_item_clicklayout).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						List<Schedule> list=new ArrayList<>();
						list.add(schedule);
						BundleModel model = new BundleModel();
						model.put("timetable", list);
						model.setFromClass(getActivity().getClass());
						model.put("item",0);
						ActivityTools.toActivityWithout(getActivity(), TimetableDetailActivity.class, model);
					}
				});
				cardLayout2.addView(view);
			}
		}
	}

	/**
	 * 获取数据
	 *
	 * @return
	 */
	public void findData() {
		ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
		if(scheduleName==null){
			scheduleName = new ScheduleName();
			scheduleName.setName("默认课表");
			scheduleName.setTime(System.currentTimeMillis());
			scheduleName.save();
			ShareTools.put(getActivity(), ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
		}

		int id = ScheduleDao.getApplyScheduleId(getActivity());
		final ScheduleName newName = DataSupport.find(ScheduleName.class, id);
		if (newName == null) return;

		FindMultiExecutor executor=newName.getModelsAsync();
		executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> models= (List<TimetableModel>) t;
                if(models!=null){
                    List<Schedule> allModels=ScheduleSupport.transform(models);
                    if(allModels!=null){
                        int curWeek = TimetableTools.getCurWeek(getActivity());
                        Calendar c = Calendar.getInstance();
                        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                        dayOfWeek = dayOfWeek - 2;
                        if (dayOfWeek == -1) dayOfWeek = 6;
                        List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
                        createCardView(list,newName);
                    }else createCardView(null,newName);
                }
            }
        });

		int id2 = ShareTools.getInt(getActivity(),ShareConstants.INT_SCHEDULE_NAME_ID2,-1);
		if(id2>0){
			final ScheduleName newName2 = DataSupport.find(ScheduleName.class, id2);
			if (newName2 !=null){
				bindView.setVisibility(View.GONE);
				FindMultiExecutor executor2=newName2.getModelsAsync();
				executor2.listen(new FindMultiCallback() {
					@Override
					public <T> void onFinish(List<T> t) {
						List<TimetableModel> models= (List<TimetableModel>) t;
						if(models!=null){
							List<Schedule> allModels=ScheduleSupport.transform(models);
							if(allModels!=null){
								int curWeek = TimetableTools.getCurWeek(getActivity());
								Calendar c = Calendar.getInstance();
								int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
								dayOfWeek = dayOfWeek - 2;
								if (dayOfWeek == -1) dayOfWeek = 6;
								List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
								createCardView2(list,newName2);
							}else createCardView2(null,newName2);
						}
					}
				});
			}else{
				showBindView();
				dayView2.setVisibility(View.GONE);
			}
		}else{
			showBindView();
			dayView2.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if(context instanceof OnSwitchPagerListener){
			onSwitchPagerListener= (OnSwitchPagerListener) context;
		}
		if(context instanceof OnUpdateCourseListener){
			onUpdateCourseListener= (OnUpdateCourseListener) context;
		}
	}

	@OnClick(R.id.id_search_school)
	public void toSearchSchool(){
		ActivityTools.toActivityWithout(getActivity(), SearchSchoolActivity.class);
	}

	@OnClick(R.id.id_func_scan)
	public void toScanActivity(){
		ActivityTools.toActivityWithout(getActivity(),ScanActivity.class);
	}

	@OnClick(R.id.id_func_multi)
	public void toMultiActivity(){
		ActivityTools.toActivityWithout(getActivity(),MultiScheduleActivity.class);
	}

	@OnClick(R.id.id_func_simport)
	public void toSimportActivity(){
		Intent intent = new Intent(getActivity(), AuthActivity.class);
		intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_IMPORT);
		startActivityForResult(intent, MainActivity.REQUEST_IMPORT);
	}

	@OnClick(R.id.id_func_setting)
	public void toSettingActivity(){
		ActivityTools.toActivityWithout(getActivity(),MenuActivity.class);
	}

	/**
	 * 接收授权页面获取的课程信息
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MainActivity.REQUEST_IMPORT && resultCode == AuthActivity.RESULT_STATUS) {
			SuperResult result= SuperUtils.getResult(data);
			if(result==null){
				Toasty.error(getActivity(), "result is null").show();
			}else{
				if(result.isSuccess()){
					List<SuperLesson> lessons = result.getLessons();
					ScheduleName newName = ScheduleDao.saveSuperShareLessons(lessons);
					if (newName != null) {
						showDialogOnApply(newName);
					} else {
						Toasty.error(getActivity(), "ScheduleName is null").show();
					}
				}else{
					Toasty.error(getActivity(), ""+result.getErrMsg()).show();
				}
			}
		}
	}

	private void showDialogOnApply(final ScheduleName name) {
		if(name==null) return;
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		builder.setMessage("你导入的数据已存储在多课表["+name.getName()+"]下!\n是否直接设置为当前课表?")
				.setTitle("课表导入成功")
				.setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						ScheduleDao.changeFuncStatus(getActivity(),true);
						ScheduleDao.applySchedule(getActivity(),name.getId());
						BroadcastUtils.refreshAppWidget(getActivity());
						if(onSwitchPagerListener!=null){
							onSwitchPagerListener.onPagerSwitch();
						}
						if(onUpdateCourseListener!=null){
							onUpdateCourseListener.onUpdateData();
						}
						findData();
						if(dialogInterface!=null){
							dialogInterface.dismiss();
						}
					}
				})
				.setNegativeButton("稍后设置",null);
		builder.create().show();
	}

	public void getValue(String id){
		TimetableRequest.getValue(getActivity(), id, new Callback<ObjResult<ValuePair>>() {
			@Override
			public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
				ObjResult<ValuePair> result=response.body();
				if(result!=null){
					if(result.getCode()==200){
						ValuePair pair=result.getData();
						if(pair!=null){
							CharSequence charSequence;
							String value=pair.getValue();
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
								charSequence = Html.fromHtml(value,Html.FROM_HTML_MODE_LEGACY);
							} else {
								charSequence = Html.fromHtml(value);
							}

							display.setText(charSequence);
						}else{
							display.setText("适配公告加载异常!");
						}
					}else{
						display.setText("Error:"+result.getMsg());
					}
				}else{
					display.setText("适配公告加载异常!");
				}
			}

			@Override
			public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
				display.setText("适配公告加载异常!");
			}
		});
	}

	@Override
	public void onUpdateNotice() {
		if(ScheduleDao.isNeedFuncUpdate(getActivity())){
			if(todayInfo.getText()!=null){
				findData();
			}
			getValue("1f088b55140a49e101e79c420b19bce6");
			ScheduleDao.changeFuncStatus(getActivity(),false);
		}
	}

	@OnClick(R.id.id_week_view)
	public void toScheduleFragment(){
		if(onSwitchPagerListener!=null){
			onSwitchPagerListener.onPagerSwitch();
		}
	}

	@OnClick(R.id.id_func_bindtable)
	public void bindTable(){
		android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(getActivity())
				.setTitle("关联课表")
				.setMessage("可在工具箱中设置隐藏情侣模式以隐藏本卡片!\n\n关联课表步骤:\n前往多课表管理页面，选择一个课表，点击【这是Ta的课表】")
				.setPositiveButton("前往关联", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						ActivityTools.toActivityWithout(getActivity(),MultiScheduleActivity.class);
					}
				})
				.setNegativeButton("取消",null);

		builder.create().show();
	}
}
