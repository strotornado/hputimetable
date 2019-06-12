package com.zhuangfei.hputimetable.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.adapter_apis.AssetTools;
import com.zhuangfei.hputimetable.event.SelectSchoolEvent;
import com.zhuangfei.hputimetable.model.GreenFruitSchool;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.qingguo.GreenFruit;
import com.zhuangfei.toolkit.tools.ActivityTools;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseSchoolActivity extends AppCompatActivity {

    @BindView(R.id.id_choose_school_listview)
    ListView listView;
    List<String> list;
    List<GreenFruitSchool> schoolList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_school);
        ButterKnife.bind(this);

        list=new ArrayList<>();
        schoolList=new ArrayList<>();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GreenFruitSchool school=findSchool(list.get(i));
                if(school!=null){
                    EventBus.getDefault().post(new SelectSchoolEvent(school));
                    finish();
                }
            }
        });

        EditText editText=findViewById(R.id.id_search_edittext);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s=charSequence.toString();
                if(s==null||s.length()==0){
                    if(schoolList!=null){
                        list.clear();
                        for(GreenFruitSchool school:schoolList){
                            list.add(school.getXxmc());
                        }
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    if(schoolList!=null){
                        list.clear();
                        for(GreenFruitSchool school:schoolList){
                            if(school.getXxmc().indexOf(s)!=-1){
                                list.add(school.getXxmc());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loadSchools();
    }

    public GreenFruitSchool findSchool(String xxmc){
        if(schoolList==null) return null;
        for(GreenFruitSchool school:schoolList){
            if(school.getXxmc().equals(xxmc)){
                return school;
            }
        }
        return null;
    }

    public Context getContext(){
        return this;
    }

    private void loadSchools() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String schoolStr= AssetTools.readAssetFile(getContext(),"schools.txt");
                TypeToken<List<GreenFruitSchool>> typeToken=new TypeToken<List<GreenFruitSchool>>(){};
                List<GreenFruitSchool> school=new Gson().fromJson(schoolStr,typeToken.getType());
                Message message=new Message();
                message.obj=school;
                message.what=0x123;
                handler.sendMessage(message);
            }
        }).start();
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.obj!=null){
                schoolList= (List<GreenFruitSchool>) msg.obj;
                if(schoolList!=null){
                    list.clear();
                    for(GreenFruitSchool school:schoolList){
                        list.add(school.getXxmc());
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        finish();
    }
}
