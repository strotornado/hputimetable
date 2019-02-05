package com.zhuangfei.hputimetable.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.MenuActivity;
import com.zhuangfei.hputimetable.MultiScheduleActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.ScanActivity;
import com.zhuangfei.hputimetable.SearchSchoolActivity;
import com.zhuangfei.hputimetable.ThemeMarketActivity;
import com.zhuangfei.hputimetable.TimetableDetailActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.listener.OnNoticeUpdateListener;
import com.zhuangfei.hputimetable.listener.OnSwitchPagerListener;
import com.zhuangfei.hputimetable.listener.OnUpdateCourseListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
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
public class ServiceStationFragment extends LazyLoadFragment{

	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_service_station, container, false);
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
