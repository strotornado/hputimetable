package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HpuRepertoryActivity extends AppCompatActivity {

    Activity context;

    @BindView(R.id.id_search_course_listview)
    ListView courseListView;
    List<Map<String, String>> courseDatas;
    SimpleAdapter courseSimpleAdapter;

    @BindView(R.id.id_search_edittext)
    EditText searchEditText;

    LinearLayout backLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        context = this;
        backLayout=findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        courseDatas = new ArrayList<>();
        courseSimpleAdapter = new SimpleAdapter(getContext(), courseDatas, R.layout.item_search_courselabel,
                new String[]{"name","show_room","show_weeks","show_day","show_teacher"},
                new int[]{R.id.id_label_name,R.id.id_label_room,R.id.id_label_weeks,R.id.id_label_day,R.id.id_label_teacher});
        courseListView.setAdapter(courseSimpleAdapter);

        searchEditText.addTextChangedListener(textWatcher);
    }

    @OnItemClick(R.id.id_search_course_listview)
    public void onCourseItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int day=1,start=1,step=1;
        if(!TextUtils.isEmpty(courseDatas.get(i).get("day"))){
            day=Integer.valueOf(courseDatas.get(i).get("day"));
        }
        if(!TextUtils.isEmpty(courseDatas.get(i).get("start"))){
            start=Integer.valueOf(courseDatas.get(i).get("start"));
        }
        if(!TextUtils.isEmpty(courseDatas.get(i).get("step"))){
            step=Integer.valueOf(courseDatas.get(i).get("step"));
        }
        BundleModel model=new BundleModel()
                .setFromClass(HpuRepertoryActivity.class)
                .put(AddTimetableActivity.KEY_NAME,courseDatas.get(i).get("name"))
                .put(AddTimetableActivity.KEY_ROOM,courseDatas.get(i).get("room"))
                .put(AddTimetableActivity.KEY_TEACHER,courseDatas.get(i).get("teacher"))
                .put(AddTimetableActivity.KEY_START,start)
                .put(AddTimetableActivity.KEY_DAY,day)
                .put(AddTimetableActivity.KEY_STEP,step)
                .put(AddTimetableActivity.KEY_WEEKS,courseDatas.get(i).get("weeks"));
        ActivityTools.toActivity(this,AddTimetableActivity.class,model);
    }

    public Activity getContext() {
        return context;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String key=charSequence.toString();
            if (TextUtils.isEmpty(key)){
                courseDatas.clear();
                courseSimpleAdapter.notifyDataSetChanged();
            }else{
                search(charSequence.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    //需要完善
    Callback<ListResult<TimetableModel>> getByNameCallback = new Callback<ListResult<TimetableModel>>() {
        @Override
        public void onResponse(Call<ListResult<TimetableModel>> call, Response<ListResult<TimetableModel>> response) {
            ListResult<TimetableModel> result = response.body();
            if (result != null) {
                int code = result.getCode();
                if (code == 200) {
                    List<TimetableModel> resultModels = result.getData();
                    if (resultModels != null) {
                        //--更新
                        updateCourseListData(resultModels);
                    }
                } else {
                    ToastTools.show(getContext(), result.getMsg());
                }
            }
        }

        @Override
        public void onFailure(Call<ListResult<TimetableModel>> call, Throwable t) {
            ToastTools.show(getContext(), t.getMessage());
        }
    };

    private void updateCourseListData(List<TimetableModel> resultModels) {
        courseDatas.clear();
        if (resultModels == null || resultModels.size() == 0) {
            courseSimpleAdapter.notifyDataSetChanged();
            return;
        }
        for (TimetableModel model : resultModels) {
            Map<String, String> map = new HashMap<>();
            String name=model.getName();
            String room=model.getRoom();
            String weeks=model.getWeeks();
            String teacher=model.getTeacher();
            int day=model.getDay();
            int start=model.getStart();
            int step=model.getStep();
            map.put("name", name);
            map.put("room", room);
            map.put("weeks", weeks);
            map.put("day", day+"");
            map.put("start",start+"");
            map.put("step",step+"");
            map.put("teacher",teacher);

            String show_day="";
            if(model.getDay()<=0||model.getStart()<=0||model.getStep()<=0){
                show_day="第X周,X-X节";
            }else{
                show_day="周"+model.getDay()+","+model.getStart()+"-"+(model.getStart()+model.getStep()-1)+"节";
            }

            String show_room=(TextUtils.isEmpty(room)?"null":room);
            String show_weeks=(TextUtils.isEmpty(weeks)?"null":weeks);

            map.put("show_room","教室:"+show_room);
            map.put("show_weeks","周次:"+show_weeks);
            map.put("show_day","节次:"+show_day);
            map.put("show_teacher","教师:"+(teacher==null?"null":teacher));

            courseDatas.add(map);
        }
        courseSimpleAdapter.notifyDataSetChanged();
    }

    public String getDay(int day){
        String str="一二三四五六七";
        return str.charAt(day-1)+"";
    }

    @OnClick(R.id.id_search_search)
    public void search() {
        String key = searchEditText.getText().toString();
        search(key);
    }

    public void search(String key) {
        if (!TextUtils.isEmpty(key)) {
            TimetableRequest.getByName(getContext(),key,getByNameCallback);
        }
    }

    public void goBack(){
        ActivityTools.toBackActivityAnim(getContext(),SearchSchoolActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
