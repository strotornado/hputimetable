package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportMajorActivity extends AppCompatActivity{

    Activity context;

    @BindView(R.id.id_set_major_listview)
    ListView listView;
    List<Map<String, String>> datas;
    SimpleAdapter simpleAdapter;

    @BindView(R.id.id_find_major_edittext)
    EditText findMajorEditText;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_major);
        ButterKnife.bind(this);
        context=this;
        inits();
        setLoadLayout(true);
        TimetableRequest.findMajor(this, "1", findMajorCallback);
    }

    private void inits() {
        datas = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(getContext(), datas, R.layout.item_major, new String[]{"major"}, new int[]{R.id.item_major_name});
        listView.setAdapter(simpleAdapter);
        findMajorEditText.addTextChangedListener(textWatcher);
    }

    public void setLoadLayout(boolean isShow){
        if(isShow){
            loadLayout.setVisibility(View.VISIBLE);
        }else {
            loadLayout.setVisibility(View.GONE);
        }
    }

    @OnItemClick(R.id.id_set_major_listview)
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String major=datas.get(i).get("major");
        setLoadLayout(true);
        TimetableRequest.getByMajor(getContext(), major, new GetByMajorCallback(major));
    }

    public Activity getContext() {
        return context;
    }

    Callback<ListResult<MajorModel>> findMajorCallback = new Callback<ListResult<MajorModel>>() {
        @Override
        public void onResponse(Call<ListResult<MajorModel>> call, Response<ListResult<MajorModel>> response) {
            ListResult<MajorModel> result = response.body();
            setLoadLayout(false);
            if (result != null) {
                int code = result.getCode();
                if (code == 200) {
                    updateListData(result.getData());
                } else {
                    ToastTools.show(getContext(), result.getMsg());
                }
            }
        }

        @Override
        public void onFailure(Call<ListResult<MajorModel>> call, Throwable t) {
            ToastTools.show(getContext(), t.getMessage());
        }
    };

    private void updateListData(List<MajorModel> resultModels) {
        datas.clear();
        if (resultModels == null || resultModels.size() == 0){
            simpleAdapter.notifyDataSetChanged();
            return;
        }

        for (MajorModel model : resultModels) {
            Map<String, String> map = new HashMap<>();
            map.put("major", model.getName());
            map.put("id", model.getId() + "");
            datas.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String key=charSequence.toString();
            if (TextUtils.isEmpty(key)){
                datas.clear();
                simpleAdapter.notifyDataSetChanged();
            }else{
                setLoadLayout(true);
                TimetableRequest.findMajor(getContext(), key, findMajorCallback);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @OnClick(R.id.id_set_major_search)
    public void search() {
        String key = findMajorEditText.getText().toString();
        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.findMajor(this, key, findMajorCallback);
        }
    }

    class GetByMajorCallback implements Callback<ObjResult<TimetableResultModel>>{
        private String major=null;
        public GetByMajorCallback(String major){
            this.major=major;
        }
        @Override
        public void onResponse(Call<ObjResult<TimetableResultModel>> call, Response<ObjResult<TimetableResultModel>> response) {
            ObjResult<TimetableResultModel> result = response.body();
            setLoadLayout(false);
            if (result != null) {
                int code = result.getCode();
                if (code == 200) {
                    ScheduleName scheduleName=new ScheduleName();
                    scheduleName.setTime(System.currentTimeMillis());
                    scheduleName.setName(major==null?"默认课表":major);
                    scheduleName.save();
                    TimetableResultModel resultModel = result.getData();
                    List<TimetableModel> haveList = resultModel.getHaveList();
                    for (TimetableModel model : haveList) {
                        model.setScheduleName(scheduleName);
                        model.save();
                    }
                    if (haveList != null && haveList.size() != 0) {
                        ShareTools.putString(getContext(), ShareConstants.KEY_CUR_TERM, haveList.get(0).getTerm());
                    }
                    Toasty.success(ImportMajorActivity.this,"已存储于["+scheduleName.getName()+"]").show();
                    ActivityTools.toActivity(ImportMajorActivity.this,MultiScheduleActivity.class);
                    finish();
                } else {
                    ToastTools.show(getContext(), result.getMsg());
                }
            }

        }

        @Override
        public void onFailure(Call<ObjResult<TimetableResultModel>> call, Throwable t) {
            ToastTools.show(getContext(), t.getMessage());
        }
    };

    @Override
    public void onBackPressed() {
        ActivityTools.toBackActivityAnim(this,MenuActivity.class);
    }
}
