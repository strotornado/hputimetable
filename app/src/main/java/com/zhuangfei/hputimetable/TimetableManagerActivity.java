package com.zhuangfei.hputimetable;

import java.util.ArrayList;
import java.util.List;

import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.appwidget.ScheduleAppWidget;
import com.zhuangfei.hputimetable.constants.ExtrasConstants;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.smartalert.core.MessageAlert;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import es.dmoral.toasty.Toasty;

public class TimetableManagerActivity extends Activity implements OnClickListener {

	private static final String TAG = "TimetableManagerActivit";

	private LinearLayout courseContainerLayout;
	private LinearLayout backLayout;

	MessageAlert messageAlert;

	private LinearLayout allCheckLayout;
	private LinearLayout deleteLayout;
	private CheckBox allCheckBox;

	List<TimetableModel> delete = new ArrayList<>();
	List<TimetableModel> courseList = new ArrayList<>();
	List<CheckBox> boxList = new ArrayList<>();

	TextView titleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable_manager);
		initView();
		initEvent();
		ShareTools.put(this, "course_update", 1);
	}

	private void initEvent() {
		backLayout.setOnClickListener(this);
		allCheckLayout.setOnClickListener(this);
		deleteLayout.setOnClickListener(this);
	}

	private void initView() {
		courseContainerLayout = (LinearLayout) findViewById(R.id.id_course_manager_container);
		backLayout = (LinearLayout) findViewById(R.id.id_back);
		allCheckLayout = (LinearLayout) findViewById(R.id.id_manager_allcheck);
		deleteLayout = (LinearLayout) findViewById(R.id.id_manager_alldelete);
		allCheckBox = (CheckBox) findViewById(R.id.id_box_allcheck);
		messageAlert = new MessageAlert(this).create();

		titleTextView=findViewById(R.id.id_manager_title);

		//获取数据
		int scheduleNameId= (int) BundleTools.getInt(this,ExtrasConstants.INT_SCHEDULE_NAME_ID,-1);
		if(scheduleNameId==-1){
			Toasty.error(this,"页面传值出现异常!",Toast.LENGTH_SHORT).show();
			ActivityTools.toBackActivityAnim(this,MultiScheduleActivity.class);
		}else{
			ScheduleName newName=DataSupport.find(ScheduleName.class,scheduleNameId);
			FindMultiExecutor executor=newName.getModelsAsync();
			executor.listen(new FindMultiCallback() {
				@Override
				public <T> void onFinish(List<T> t) {
					List<TimetableModel> modelList= (List<TimetableModel>) t;
					if(modelList!=null){
						courseList.clear();
						courseList.addAll(modelList);
					}
					if(courseList.size()==0){
						Toasty.info(TimetableManagerActivity.this,"没有课程！",Toast.LENGTH_SHORT).show();
					}
					addCourseItemView();
				}
			});
		}
	}

	private void addCourseItemView() {
		courseContainerLayout.removeAllViews();

		LinearLayout containerLayout;
		TextView nameTextView, roomTextView, weeksTextView, dayTextView, teacherTextView;

		if(courseList!=null){
			String scheduleName=BundleTools.getString(this,ExtrasConstants.STRING_SCHEDULE_NAME,"课程管理");
			titleTextView.setText(scheduleName+"("+courseList.size()+")");
		}

		for (int i = 0; i < courseList.size(); i++) {
			final TimetableModel course = courseList.get(i);
			final View view = LayoutInflater.from(this).inflate(R.layout.item_coursemanager_layout, null);
			containerLayout = (LinearLayout) view.findViewById(R.id.id_container);
			nameTextView = (TextView) view.findViewById(R.id.id_manager_name);
			roomTextView = (TextView) view.findViewById(R.id.id_manager_room);
			weeksTextView = (TextView) view.findViewById(R.id.id_manager_weeks);
			dayTextView = (TextView) view.findViewById(R.id.id_manager_day);
			teacherTextView = (TextView) view.findViewById(R.id.id_manager_teacher);
			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.id_manager_check);

			checkBox.setTag(course);
			boxList.add(checkBox);

			containerLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (checkBox.isChecked()) {
						checkBox.setChecked(false);
						delete.remove(course);
					} else {
						checkBox.setChecked(true);
						delete.add(course);
					}

					if (courseList.size() != 0 && courseList.size() == delete.size()) {
						allCheckBox.setChecked(true);
					} else {
						allCheckBox.setChecked(false);
					}
				}
			});

			nameTextView.setText(course.getName());

			if(!TextUtils.isEmpty(course.getWeeks())){
				List<Integer> list=TimetableTools.getWeekList(course.getWeeks());
				weeksTextView.setText("周次:\t" + (list==null?"null":list.toString()));
			}else{
				weeksTextView.setText("周次:\t" + (course.getWeekList()==null?"null":course.getWeekList().toString()));
			}

			roomTextView.setText("教室:\t" + course.getRoom());
			dayTextView.setText("节次:\t周" + getDay(course.getDay()) + ",\t" + course.getStart() + "-"
					+ (course.getStart() + course.getStep() - 1) + "节");
			teacherTextView.setText("教师:\t" + course.getTeacher());

			courseContainerLayout.addView(view);
		}
	}

	public String getDay(int day) {
		String str = "一二三四五六七";
		return str.charAt(day - 1) + "";
	}

	@Override
	public void onBackPressed() {
		ActivityTools.toBackActivityAnim(this, MultiScheduleActivity.class);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.id_back:
				ActivityTools.toBackActivityAnim(TimetableManagerActivity.this, MultiScheduleActivity.class);
				break;

			case R.id.id_manager_allcheck:
				allCheck();
				break;
			case R.id.id_manager_alldelete:
				if (delete.size() == 0) Toasty.warning(this, "没有选中的课程", Toast.LENGTH_SHORT).show();
				else {
					messageAlert.setCancelEnable(true).setTitle("删除课程").setContent("你是否确定要删除选中的课程?")
							.setOnMessageAlertListener(new OnMessageAlertAdapter() {
								@Override
								public void onConfirm(MessageAlert messageAlert) {
									allDelete();

									messageAlert.hide();
								}
							}).show();
				}
				break;
			default:
				break;
		}
	}

	private void allDelete() {
		for(TimetableModel model:delete){
			model.delete();
		}
		courseList.removeAll(delete);
		delete.clear();
		boxList.clear();
		allCheckBox.setChecked(false);
		Toasty.success(this, "删除成功!", Toast.LENGTH_SHORT).show();
		ScheduleDao.changeFuncStatus(this,true);
		ScheduleDao.changeStatus(this,true);
		addCourseItemView();
		BroadcastUtils.refreshAppWidget(this);
	}

	private void allCheck() {
		if (allCheckBox.isChecked()) {
			for (int i = 0; i < boxList.size(); i++) {
				CheckBox checkBox = boxList.get(i);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
					TimetableModel model= (TimetableModel) checkBox.getTag();
					delete.remove(model);
				}
			}
			allCheckBox.setChecked(false);
		} else {
			for (int i = 0; i < boxList.size(); i++) {
				CheckBox checkBox = boxList.get(i);
				if (!checkBox.isChecked()) {
					checkBox.setChecked(true);
					TimetableModel model= (TimetableModel) checkBox.getTag();
					delete.add(model);
				}
			}
			allCheckBox.setChecked(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (messageAlert != null) {
			messageAlert.dimiss();
		}
	}
}
