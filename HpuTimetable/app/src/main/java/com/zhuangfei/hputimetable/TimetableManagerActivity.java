package com.zhuangfei.hputimetable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.smartalert.core.ImageAlert;
import com.zhuangfei.smartalert.core.MessageAlert;
import com.zhuangfei.smartalert.listener.OnImageAlertListener;
import com.zhuangfei.smartalert.listener.OnMessageAlertAdapter;
import com.zhuangfei.smartalert.listener.OnMessageAlertListener;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

public class TimetableManagerActivity extends Activity implements OnClickListener {

	private static final String TAG = "TimetableManagerActivit";

	private LinearLayout courseContainerLayout;
	private LinearLayout backLayout;
	private TextView addLayout;

	MessageAlert messageAlert;

	private LinearLayout allCheckLayout;
	private LinearLayout deleteLayout;
	private CheckBox allCheckBox;

	List<CheckBox> boxs = new ArrayList<>();
	List<TimetableModel> courses = new ArrayList<>();
	List<TimetableModel> courseList = new ArrayList<>();

	private String term;
	private String major;

	TextView titleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable_manager);
		initView();
		initEvent();
		addCourseItemView();
		ShareTools.put(this, "course_update", 1);
	}

	private void initEvent() {
		backLayout.setOnClickListener(this);
		addLayout.setOnClickListener(this);
		allCheckLayout.setOnClickListener(this);
		deleteLayout.setOnClickListener(this);
	}

	private void initView() {
		courseContainerLayout = (LinearLayout) findViewById(R.id.id_course_manager_container);
		backLayout = (LinearLayout) findViewById(R.id.id_back);
		addLayout = (TextView) findViewById(R.id.id_add);
		allCheckLayout = (LinearLayout) findViewById(R.id.id_manager_allcheck);
		deleteLayout = (LinearLayout) findViewById(R.id.id_manager_alldelete);
		allCheckBox = (CheckBox) findViewById(R.id.id_box_allcheck);
		messageAlert = new MessageAlert(this).create();

		titleTextView=findViewById(R.id.id_manager_title);

		term = (String) ShareTools.get(this, ShareConstants.KEY_CUR_TERM, "term");
		major=ShareTools.getString(this,ShareConstants.KEY_MAJOR_NAME,"major");
		courseList = DataSupport.where("term=? and major=?", term,major).find(TimetableModel.class,true);
		Log.d(TAG, "initView: "+courseList.size());
	}

	private void addCourseItemView() {
		courseContainerLayout.removeAllViews();
		Log.d(TAG, "addCourseItemView: "+courseList.size());

		LinearLayout containerLayout;
		TextView nameTextView, roomTextView, weeksTextView, dayTextView, teacherTextView;

		if(courseList!=null){
			titleTextView.setText("课程管理("+courseList.size()+")");
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

			checkBox.setTag(view);
			boxs.add(checkBox);

			containerLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (checkBox.isChecked()) {
						checkBox.setChecked(false);
						courses.remove(course);
					} else {
						checkBox.setChecked(true);
						courses.add(course);
					}

					if (courseList.size() != 0 && courseList.size() == courses.size()) {
						allCheckBox.setChecked(true);
					} else {
						allCheckBox.setChecked(false);
					}
				}
			});

			if(course.getTag()==1){
				nameTextView.setText("[添加]"+course.getName());
				nameTextView.setTextColor(getResources().getColor(R.color.app_gold));
			}else{
				nameTextView.setText(course.getName());
			}

			roomTextView.setText("教室:\t" + course.getRoom());
			weeksTextView.setText("周次:\t" + course.getWeeks());
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
		ActivityTools.toBackActivityAnim(this, MenuActivity.class);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.id_back:
				ActivityTools.toBackActivityAnim(TimetableManagerActivity.this, MenuActivity.class);
				break;

			case R.id.id_add:
				ActivityTools.toActivity(TimetableManagerActivity.this, AddTimetableActivity.class);
				finish();
				break;
			case R.id.id_manager_allcheck:
				allCheck();
				break;
			case R.id.id_manager_alldelete:
				if (courses.size() == 0) Toast.makeText(this, "没有选中的课程", Toast.LENGTH_SHORT).show();
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
		List<TimetableModel> dataModels = DataSupport.where("term=? and major=?", term,major).find(TimetableModel.class,true);
		List<TimetableModel> deleteModels = new ArrayList<>();
		for (int i = 0; i < courses.size(); i++) {
			TimetableModel c = courses.get(i);
			for (int m = 0; m < dataModels.size(); m++) {
				TimetableModel model = dataModels.get(m);
				if ((model.getTerm().equals(c.getTerm()))
						&&(model.getName().equals(c.getName()))
						&&(model.getDay()==c.getDay())
						&&(model.getStart()==c.getStart())
						&&(model.getStep()==c.getStep())
						&&(model.getRoom().equals(c.getRoom()))
						&&(model.getTeacher().equals(c.getTeacher()))) {
					if(model.getWeekList()==null&&c.getWeekList()==null){
						deleteModels.add(model);
					}

					if(model.getWeekList()!=null&&c.getWeekList()!=null&&model.getWeekList().containsAll(c.getWeekList())&&c.getWeekList().containsAll(model.getWeekList())){
						deleteModels.add(model);
					}
				}
			}
		}
		for(TimetableModel delete:deleteModels){
			delete.delete();
		}

		courseList.removeAll(courses);
		courses.clear();
		allCheckBox.setChecked(false);
		Toast.makeText(this, "删除成功!", Toast.LENGTH_SHORT).show();
		addCourseItemView();
	}

	private void allCheck() {
		if (allCheckBox.isChecked()) {
			for (int i = 0; i < boxs.size(); i++) {
				CheckBox checkBox = boxs.get(i);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
					courses.remove(courseList.get(i));
				}
			}
			allCheckBox.setChecked(false);
		} else {
			for (int i = 0; i < boxs.size(); i++) {
				CheckBox checkBox = boxs.get(i);
				if (!checkBox.isChecked()) {
					checkBox.setChecked(true);
					courses.add(courseList.get(i));
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
