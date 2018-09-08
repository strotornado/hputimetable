package com.zhuangfei.hputimetable.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuangfei.hputimetable.CreateScheduleNameActivity;
import com.zhuangfei.hputimetable.ModifyScheduleNameActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.TimetableManagerActivity;
import com.zhuangfei.hputimetable.adapter.MultiScheduleAdapter;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.constants.ExtrasConstants;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import es.dmoral.toasty.Toasty;

public class MultiScheduleFragment extends LazyLoadFragment {
    private static final String TAG = "MultiScheduleFragment";

    @BindView(R.id.id_multi_listview)
    ListView listView;
    MultiScheduleAdapter adapter;
    List<ScheduleName> nameList;
    List<Integer> scheduleCounts;

    @BindView(R.id.id_title)
    TextView titleTextView;

    private View mView;
    private Activity context;

    @BindView(R.id.id_loadlayout)
    public LinearLayout loadLayout;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_multi_schedule, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this,view);
        inits();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        Log.d(TAG, "lazyLoad: ");
        getData();
    }

    private void inits() {
        context=getActivity();
        scheduleCounts = new ArrayList<>();
        nameList = new ArrayList<>();
        scheduleCounts = new ArrayList<>();
        adapter = new MultiScheduleAdapter(context, nameList, scheduleCounts);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
    }

    public void apply(ScheduleName scheduleName) {
        if (scheduleName == null) return;
        int id = scheduleName.getId();
        ShareTools.put(context, "course_update", 1);
        ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);
        Toasty.success(context, "切换课表成功").show();
        adapter.notifyDataSetChanged();
    }

    private void deleteScheduleName(final ScheduleName scheduleName) {
        if (scheduleName == null) return;
        if (scheduleName.getName().equals("默认课表")) {
            Toasty.error(context, "默认课表，不允许删除").show();
            return;
        }
        if (scheduleName.getModels() == null || scheduleName.getModels().size() == 0) {
            scheduleName.delete();
            getData();
            Toasty.success(context, "删除成功").show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("删除[" + scheduleName.getName() + "]")
                    .setMessage("本课表下有课，是否确认删除？")
                    .setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            scheduleName.delete();
                            int cur = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
                            if (cur == scheduleName.getId()) {
                                ScheduleName newName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
                                if (newName != null) {
                                    ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, newName.getId());
                                }
                            }
                            getData();
                            Toasty.success(context, "删除成功").show();
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", null);

            builder.create().show();
        }
    }


    public void showListDialog(final int pos) {
        final String items[] = {"课表详情", "修改课表名", "删除本课表", "设置为当前课表"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择操作");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        ActivityTools.toActivity(context,
                                TimetableManagerActivity.class,
                                new BundleModel()
                                        .put(ExtrasConstants.INT_SCHEDULE_NAME_ID,
                                                nameList.get(pos).getId())
                                        .put(ExtrasConstants.STRING_SCHEDULE_NAME, nameList.get(pos).getName()));
                        break;
                    case 1:
                        modifyScheduleName(nameList.get(pos));
                        break;
                    case 2:
                        deleteScheduleName(nameList.get(pos));
                        break;
                    case 3:
                        apply(nameList.get(pos));
                        getData();
                        BroadcastUtils.refreshAppWidget(context);
                        break;
                }
            }
        });
        builder.setNegativeButton("取消操作", null);
        builder.create().show();
    }

    private void modifyScheduleName(ScheduleName scheduleName) {
        if (scheduleName == null) return;
        BundleModel model = new BundleModel()
                .put(ModifyScheduleNameActivity.STRING_EXTRA_NAME, scheduleName.getName())
                .put(ModifyScheduleNameActivity.INT_EXTRA_ID, scheduleName.getId());
        ActivityTools.toActivity(context, ModifyScheduleNameActivity.class, model);
    }

    public void getData() {
        loadLayout.setVisibility(View.VISIBLE);
        nameList.clear();

        FindMultiExecutor executor=DataSupport.order("time desc").findAsync(ScheduleName.class);
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(final List<T> t) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nameList.addAll((Collection<? extends ScheduleName>) t);
                        titleTextView.setText("多课表(" + nameList.size() + ")");
                        scheduleCounts.clear();

                        int index = -1;
                        int cur = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
                        for (int i = 0; i < nameList.size(); i++) {
                            ScheduleName nameBean = nameList.get(i);
                            if (cur != -1 && cur == nameBean.getId()) {
                                index = i;
                            }
                            scheduleCounts.add(nameBean.getModels().size());
                        }

                        if (index != -1) {
                            ScheduleName curName = nameList.get(index);
                            nameList.remove(index);
                            nameList.add(0, curName);
                            scheduleCounts.remove(index);
                            scheduleCounts.add(0, curName.getModels().size());
                        }

                        adapter.notifyDataSetChanged();
                        loadLayout.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @OnClick(R.id.cv_create)
    public void toCreateScheduleNameActivity() {
        ActivityTools.toActivity(context, CreateScheduleNameActivity.class);
    }

    @OnItemClick(R.id.id_multi_listview)
    public void toManagerActivity(int pos) {
        showListDialog(pos);
    }
}
