package com.zhuangfei.hputimetable.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetTimeActivity extends AppCompatActivity {

    @BindView(R.id.id_time_edit)
    EditText timeEdit;

    @BindView(R.id.id_time_display)
    TextView displayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setDefaultTime();
    }

    public void setDefaultTime(){
        String time= ShareTools.getString(this,"schedule_time",null);
        if(TextUtils.isEmpty(time)){
            String s="08:00-08:50\n09:00-09:50\n"
                    +"10:10-11:00\n11:10-12:00\n"
                    +"15:00-15:50\n16:00-16:50\n"
                    +"17:00-17:50\n18:00-18:50\n"
                    +"19:30-20:20\n20:30-21:20";
            timeEdit.setText(s);
            ToastTools.show(this,"未设置，已展示默认时间");
        }else{
            timeEdit.setText(time);
        }
    }
    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }

    @OnClick(R.id.id_set_time)
    public void onSetTimeButtonClicked(){
        displayText.setText("");
        String timeString=timeEdit.getText().toString();
        if(TextUtils.isEmpty(timeString)){
            ToastTools.show(this,"不允许为空");
            return;
        }
        String[] timeArray=timeString.split("\\n");
        List<String> startTimeList=new ArrayList<>();
        List<String> endTimeList=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        if(timeArray==null){
            for(String item:timeArray){
                if(item==null||item.indexOf("-")==-1){
                    ToastTools.show(this,"解析出错：某行没有横杠");
                    return;
                }
                String[] lineArray=item.split("-");
                if(lineArray==null||lineArray.length!=2){
                    ToastTools.show(this,"解析出错：数组长度!=2");
                    return;
                }
                try {
                    Date date1=sdf.parse(lineArray[0]);
                    Date date2=sdf.parse(lineArray[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                    ToastTools.show(this,"解析出错：不符合规范");
                    return;
                }
                startTimeList.add(lineArray[0]);
                endTimeList.add(lineArray[1]);
            }
        }

        StringBuffer sb=new StringBuffer();
        sb.append("解析结果:\n");
        for(int i=0;i<startTimeList.size();i++){
            sb.append("第"+(i+1)+"节："+startTimeList.get(i)+"-"+endTimeList.get(i));
            sb.append("\n");
        }
        displayText.setText(sb.toString());

        if(startTimeList.size()!=endTimeList.size()||startTimeList.size()<5){
            ToastTools.show(this,"行数太少，最少5行");
            return;
        }

        ShareTools.putString(this,"schedule_time",timeString);
        ToastTools.show(this,"设置成功!");
    }
}
