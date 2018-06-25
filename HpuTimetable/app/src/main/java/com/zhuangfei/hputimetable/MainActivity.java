package com.zhuangfei.hputimetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.listener.TimeSlideAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.timetable.view.WeekView;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity{

    private static final String TAG = "MainActivity";
    private Activity context;

    @BindView(R.id.id_timetableView)
    public TimetableView mTimetableView;

    @BindView(R.id.id_weekview)
    public WeekView mWeekView;

    @BindView(R.id.id_title)
    public TextView mTitleTextView;

    private List<Schedule> schedules;

    public Activity getContext() {
        return context;
    }

    String major="软件15-1";
    boolean isShow=false;
    int target;
    int redColor;

    @BindView(R.id.id_main_major)
    TextView majorTextView;

    @BindView(R.id.id_main_menu)
    ImageView menuImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        inits();
        adjustAndGetData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int v=ShareTools.getInt(this, "course_update", 0);
        if(v==1){
            String term=ShareTools.getString(getContext(),ShareConstants.KEY_CUR_TERM,"term");
            List<TimetableModel> dataModels = DataSupport.where("term=? and major=?", term,major).find(TimetableModel.class);
            if(dataModels==null||dataModels.size()==0){
                TimetableRequest.getByMajor(getContext(), major,getByMajorCallback );
            }else{
                mTimetableView.setData(ScheduleSupport.transform(dataModels)).updateView();
                mWeekView.setData(ScheduleSupport.transform(dataModels)).showView();
            }

            mTimetableView.updateView();
            ShareTools.put(this, "course_update", 0);
        }
        try{
            mTimetableView.getOnDateBuildListener().onHighLight();
        }catch (Exception e){

        }

    }

    private void inits() {
        context=this;
        redColor=getResources().getColor(R.color.app_red);
        menuImageView.setColorFilter(Color.GRAY);
        major= ShareTools.getString(getContext(),ShareConstants.KEY_MAJOR_NAME,"软件15-1");
        schedules=new ArrayList<>();

        int curWeek=ShareTools.getInt(getContext(),ShareConstants.KEY_CUR_WEEK,1);

        //设置周次选择属性
        mWeekView.setData(schedules)
                .setCurWeek(curWeek)
                .setOnWeekItemClickedListener(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int curWeek) {
                        if(mTimetableView.getDataSource()!=null){
                            mTimetableView.changeWeekOnly(curWeek);
                        }
                    }
                })
                .setOnWeekLeftClickedListener(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .showView();

        mWeekView.setVisibility(View.GONE);

        String[] times = new String[]{
                "8:00", "9:00", "10:10", "11:10",
                "15:00", "16:00", "17:00", "18:00",
                "19:30", "20:30"
        };
        TimeSlideAdapter slideAdapter = new TimeSlideAdapter();
        slideAdapter.setTimes(times);

        mTimetableView.getScheduleManager()
                .setMaxSlideItem(10)
                .setMarLeft(ScreenUtils.dip2px(this,2))
                .setMarTop(ScreenUtils.dip2px(this,2))
                .setOnSlideBuildListener(slideAdapter)
                .setOnItemClickListener(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model=new BundleModel();
                        model.put("timetable",scheduleList);
                        model.setFromClass(MainActivity.class);
                        ActivityTools.toActivity(getContext(),TimetableDetailActivity.class,model);
                    }
                });

        mTimetableView.setCurWeek(curWeek)
                .setOnWeekChangedListener(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        String text = "第" + curWeek + "周";
                        mTitleTextView.setText(text);
                    }
                })
                .showView();

    }

    /**
     * 周次选择布局的左侧被点击时回调
     */
    protected  void onWeekLeftLayoutClicked(){
        final String items[] = new String[20];
        for(int i=0;i<20;i++){
            items[i]="第"+(i+1)+"周";
        }
        target=-1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.getCurWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target=i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: "+which);
                if(target!=-1){
                    mWeekView.setCurWeek(target+1).updateView();
                    ShareTools.putInt(getContext(),ShareConstants.KEY_CUR_WEEK,target+1);
                    mTimetableView.changeWeekForce(target+1);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void adjustAndGetData() {
        String term=ShareTools.getString(getContext(),ShareConstants.KEY_CUR_TERM,"term");
        List<TimetableModel> dataModels = DataSupport.where("term=? and major=?", term,major).find(TimetableModel.class);

        Log.d(TAG, "adjustAndGetData: "+dataModels);
        if(dataModels==null||dataModels.size()==0){
            TimetableRequest.getByMajor(getContext(), major,getByMajorCallback );
        }else{
            mTimetableView.setData(ScheduleSupport.transform(dataModels)).updateView();
            mWeekView.setData(ScheduleSupport.transform(dataModels)).showView();
        }
        majorTextView.setText(major);
        majorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),major,Toast.LENGTH_SHORT).show();
            }
        });
    }

    Callback<ObjResult<TimetableResultModel>> getByMajorCallback=new Callback<ObjResult<TimetableResultModel>>() {
        @Override
        public void onResponse(Call<ObjResult<TimetableResultModel>> call, Response<ObjResult<TimetableResultModel>> response) {
            ObjResult<TimetableResultModel> result=response.body();
            if(result!=null){
                int code=result.getCode();
                if(code==200){
                    Log.d(TAG, "onResponse: ");
                    TimetableResultModel resultModel=result.getData();
                    List<TimetableModel> haveList=resultModel.getHaveList();
                    for(TimetableModel model:haveList){
                        model.setTag(0);
                        model.save();
                    }
                    if(haveList!=null&&haveList.size()!=0){
                        ShareTools.putString(getContext(),ShareConstants.KEY_CUR_TERM,haveList.get(0).getTerm());
                    }
                    mTimetableView.setData(ScheduleSupport.transform(haveList)).updateView();
                    mWeekView.setData(ScheduleSupport.transform(haveList)).showView();
                }else{
                    ToastTools.show(getContext(),result.getMsg());
                }
            }

        }

        @Override
        public void onFailure(Call<ObjResult<TimetableResultModel>> call, Throwable t) {
            ToastTools.show(getContext(),t.getMessage());
        }
    };


    @OnClick(R.id.id_main_menu)
    public void toMenuActivity(){
        ActivityTools.toActivity(getContext(), MenuActivity.class);
    }

    @OnClick(R.id.id_title)
    public void onTitleClicked(){
        Log.d(TAG, "onTitleClicked: "+mWeekView.isShowing());
        if(isShow){
            mWeekView.setVisibility(View.GONE);
            isShow=false;
            mTimetableView.changeWeekForce(mTimetableView.getCurWeek());
        }else{
            mWeekView.setVisibility(View.VISIBLE);
            isShow=true;
        }
    }
}
