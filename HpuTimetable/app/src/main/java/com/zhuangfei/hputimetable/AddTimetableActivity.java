package com.zhuangfei.hputimetable;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.smartalert.core.WeekAlert;
import com.zhuangfei.smartalert.listener.OnWeekAlertListener;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTimetableActivity extends Activity implements OnWeekAlertListener{

    private static final String TAG = "AddTimetableActivity";
    @BindView(R.id.id_addcourse_name)
    public EditText nameEditText;

    @BindView(R.id.id_addcourse_room)
    public EditText roomEditText;

    @BindView(R.id.id_addcourse_teacher)
    public EditText teacherEditText;

    @BindView(R.id.id_addcourse_weeks_textview)
    public TextView weekTextView;

    @BindView(R.id.id_addcourse_days_textview)
    public TextView dayTextView;

    private List<Integer> weeksList = new ArrayList<>();
    private int day=-1;
    private int start=-1;
    private int step=-1;
    private String weeks;

    ArrayList<String> optionsItems;
    ArrayList<ArrayList<String>> options2Items;
    ArrayList<ArrayList<ArrayList<String>>> options3Items;

    private WeekAlert weekAlert;

    private Class returnClass=TimetableManagerActivity.class;

    LinearLayout backLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);
        ButterKnife.bind(this);
        initsData();
    }

    private void initsData() {
        backLayout=findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        weekAlert=new WeekAlert(this);
        weekAlert.setTitle("选择周数")
                .setCancelEnable(true)
                .setOnWeekAlertListener(this)
                .create();

        String[] dayArray = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String[] startArray = {"第一节", "第二节", "第三节", "第四节", "第五节", "第六节", "第七节", "第八节", "第九节", "第十节", "第十一节", "第十二节"};
        optionsItems = new ArrayList<>();
        options2Items = new ArrayList<>();
        options3Items = new ArrayList<>();
        for (int i = 0; i < dayArray.length; i++) {
            optionsItems.add(dayArray[i]);
            ArrayList<String> optionItems2_01 = new ArrayList<>();
            ArrayList<ArrayList<String>> optionItems3_01 = new ArrayList<>();

            for (int j = 0; j < startArray.length; j++) {
                optionItems2_01.add(startArray[j]);
                ArrayList<String> optionItems3_01_01 = new ArrayList<>();
                for (int m = j; m < startArray.length; m++) {
                    optionItems3_01_01.add(startArray[m]);
                }
                optionItems3_01.add(optionItems3_01_01);
            }
            options2Items.add(optionItems2_01);
            options3Items.add(optionItems3_01);
        }

        returnClass=BundleTools.getFromClass(this,TimetableManagerActivity.class);
        String name=BundleTools.getString(this,"name","");
        String room=BundleTools.getString(this,"room","");
        String teacher=BundleTools.getString(this,"teacher","");
        start= Integer.parseInt(BundleTools.getString(this,"start","-1"));
        step= Integer.parseInt(BundleTools.getString(this,"step","-1"));
        day= Integer.parseInt(BundleTools.getString(this,"day","-1"));
        weeks=BundleTools.getString(this,"weeks","");
        nameEditText.setText(""+name);
        roomEditText.setText(""+room);
        teacherEditText.setText(teacher);

        if(start>0&&step>0&&day>0){
            //返回的分别是三个级别的选中位置
            String tx = optionsItems.get(day-1)
                    + ","+start+ "-"+(start+step-1)+"节";
            dayTextView.setText(tx);
        }

        if(weeks!=null&&!TextUtils.isEmpty(weeks)){
            try{
                weeksList=TimetableTools.getWeekList(weeks);
                weekTextView.setText(weeks);
            }catch (Exception e){

            }
        }
    }

    @OnClick(R.id.id_addcourse_savebutton)
	public void save(){
        String name=nameEditText.getText().toString().trim();
        String teacher=teacherEditText.getText().toString().trim();
        String room=roomEditText.getText().toString().trim();
        String term=ShareTools.getString(this, ShareConstants.KEY_CUR_TERM,"term");
        if(start<=0||step<=0||day<=0|| TextUtils.isEmpty(name)||TextUtils.isEmpty(teacher)||TextUtils.isEmpty(room)||weeksList==null||(weeksList.size()==0)){
            Toast.makeText(this, "不可为空!",Toast.LENGTH_SHORT).show();
            return;
        }

        String major=ShareTools.getString(this,ShareConstants.KEY_MAJOR_NAME,"major");
        TimetableModel model=new TimetableModel(term,name,room,teacher,weeksList,start,step,day,-1,"");
        model.setTag(1);
        model.setMajor(major);
        model.setWeeks(weeks);
        boolean isSuccess=model.save();

        if(isSuccess){
            Toast.makeText(this, "保存成功",Toast.LENGTH_SHORT).show();
            ActivityTools.toBackActivityAnim(this,returnClass);
        }else{
            Toast.makeText(this, "保存失败!",Toast.LENGTH_SHORT).show();
        }
	}

    @OnClick(R.id.id_addcourse_days)
    public void showOptionDialog() {
        //条件选择器
        OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                day=options1+1;
                start=option2+1;
                step=options3+1;
                //返回的分别是三个级别的选中位置
                String tx = optionsItems.get(day-1)
                        + ","+start+ "-"+(start+step-1)+"节";
                dayTextView.setText(tx);
            }
        }).setSubmitText("确定")//确定按钮文字
                .setCancelText("取消")//取消按钮文字
                .setTitleText("节次选择")//标题
                .build();

        pvOptions.setPicker(optionsItems, options2Items, options3Items);
        pvOptions.show();
    }

    @OnClick(R.id.id_addcourse_weeks)
    public void showWeekAlert() {
        weekAlert.setDefault(weeksList)
                .show();
    }

    @Override
    public void onBackPressed() {
        ActivityTools.toBackActivityAnim(this,returnClass);
    }

    @Override
    public void onConfirm(WeekAlert messageAlert, List<Integer> result) {
        weeks="";
        for(int i=0;i<result.size();i++){
            weeks+=result.get(i);
            if(i!=result.size()-1) weeks+=",";
        }
        messageAlert.hide();
        weeksList=result;
        if(result.size()==0) weekTextView.setText("选择周数");
        else{
            weekTextView.setText(weeks+"周上");
        }
    }

    @Override
    public void onCancel(WeekAlert messageAlert) {
        messageAlert.hide();
    }

    public void goBack(){
        ActivityTools.toBackActivityAnim(this,MenuActivity.class);
    }

}
