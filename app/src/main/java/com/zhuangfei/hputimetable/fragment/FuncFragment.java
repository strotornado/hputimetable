package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.AdapterTipActivity;
import com.zhuangfei.hputimetable.HpuRepertoryActivity;
import com.zhuangfei.hputimetable.ImportMajorActivity;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.SearchSchoolActivity;
import com.zhuangfei.hputimetable.UploadHtmlActivity;
import com.zhuangfei.hputimetable.WebViewActivity;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Administrator 刘壮飞
 * 
 */
@SuppressLint({ "NewApi", "ValidFragment" })
public class FuncFragment extends Fragment{

	private View mView;

	@BindView(R.id.id_cardview_layout)
	LinearLayout cardLayout;

	@BindView(R.id.id_cardview_today)
	TextView todayInfo;

	OnSwitchPagerListener onSwitchPagerListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_func, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this,view);
		inits();
	}

	private void inits() {
		createCardView();
	}

	public void createCardView(){
		cardLayout.removeAllViews();
		SimpleDateFormat sdf2=new SimpleDateFormat("EEEE");
		int curWeek = TimetableTools.getCurWeek(getActivity());
		todayInfo.setText("第"+curWeek+"周  "+sdf2.format(new Date()));

		List<Schedule> models=findTodayData(getActivity());
		for(Schedule schedule:models){
			LayoutInflater inflater=LayoutInflater.from(getContext());
			View view=inflater.inflate(R.layout.item_cardview,null ,false);
			TextView startText=view.findViewById(R.id.id_item_start);
			TextView infoText=view.findViewById(R.id.id_item_info);
			infoText.setText(schedule.getName()+"@"+schedule.getRoom());
			startText.setText(schedule.getStart() + "-" + (schedule.getStart() + schedule.getStep() - 1));
			view.findViewById(R.id.id_item_clicklayout).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(onSwitchPagerListener!=null){
						onSwitchPagerListener.onPagerSwitch();
					}
				}
			});
			cardLayout.addView(view);
		}
	}

	/**
	 * 获取数据
	 *
	 * @return
	 */
	public List<Schedule> findData(Context context) {
		if (context == null) return null;
		int id = ScheduleDao.getApplyScheduleId(getActivity());
		ScheduleName newName = DataSupport.find(ScheduleName.class, id);
		List<TimetableModel> models=newName.getModels();
		if(models==null) return null;
		return ScheduleSupport.transform(models);
	}

	public List<Schedule> findTodayData(Context context) {
		List<Schedule> allModels = findData(context);
		if (allModels == null) return new ArrayList<>();
		int curWeek = TimetableTools.getCurWeek(context);
		Calendar c = Calendar.getInstance();
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = dayOfWeek - 2;
		if (dayOfWeek == -1) dayOfWeek = 6;
		List<Schedule> list = ScheduleSupport.getHaveSubjectsWithDay(allModels, curWeek, dayOfWeek);
		if(list==null) return new ArrayList<>();
		return list;
	}

	@OnClick(R.id.id_menu_search)
	public void toSearchActivity() {
		ActivityTools.toActivity(getActivity(), HpuRepertoryActivity.class);
	}

	@OnClick(R.id.id_menu_require_space)
	public void requireSpace() {
		ActivityTools.toActivity(getActivity(), WebViewActivity.class,
				new BundleModel().setFromClass(MainActivity.class)
						.put("title", "专区申请")
						.put("url", "https://github.com/zfman/hputimetable/wiki/%E5%AD%A6%E6%A0%A1%E4%B8%93%E5%8C%BA%E7%94%B3%E8%AF%B7%E7%96%91%E9%97%AE%E8%A7%A3%E7%AD%94"));
	}

	@OnClick(R.id.id_menu_changeclass)
	public void changeClass() {
		ActivityTools.toActivity(getActivity(), ImportMajorActivity.class);
	}

	@OnClick(R.id.id_menu_notice)
	public void onNoticeLayoutCLicked() {
		BundleModel model = new BundleModel();
		model.setFromClass(MainActivity.class)
				.put("title", "最新公告")
				.put("url", "https://vpn.hpu.edu.cn")
				.put("isUse", 1);
		ActivityTools.toActivity(getActivity(), WebViewActivity.class, model);
	}

	@OnClick(R.id.id_menu_score)
	public void score() {
		int show = ShareTools.getInt(getActivity(), ShareConstants.KEY_SHOW_ALERTDIALOG, 1);
		if (show == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("查询指南")
					.setMessage("步骤如下：\n\n1.点击[确认]\n2.登录VPN,若失败,可以使用其他同学的校园网账号,vpn密码默认是身份证后六位" +
							"\n3.登陆教务处,输入个人教务处账号,密码默认为学号\n4.登陆成功后,网页无法点击,这是正常现象." +
							"\n4.此时,点击右上角,选择[兼容模式菜单],选择需要的功能即可\n");

			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					BundleModel model = new BundleModel();
					model.setFromClass(MainActivity.class);
					model.put("title", "成绩查询");
					model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
					ShareTools.putInt(getActivity(), ShareConstants.KEY_SHOW_ALERTDIALOG, 0);
					ActivityTools.toActivity(getActivity(), WebViewActivity.class, model);
				}
			}).setNegativeButton("取消", null);
			builder.create().show();
		} else {
//			BundleModel model = new BundleModel();
//			model.setFromClass(MainActivity.class);
//			model.put("title", "成绩查询");
//			model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
//			ActivityTools.toActivity(getActivity(), WebViewActivity.class, model);
			ActivityTools.toActivity(getActivity(), AdapterTipActivity.class);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if(context instanceof OnSwitchPagerListener){
			onSwitchPagerListener= (OnSwitchPagerListener) context;
		}
	}

	@OnClick(R.id.id_search_school)
	public void toSearchSchool(){
		ActivityTools.toActivity(getActivity(), SearchSchoolActivity.class);
	}
}
