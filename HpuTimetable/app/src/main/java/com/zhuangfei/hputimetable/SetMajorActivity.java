package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetMajorActivity extends AppCompatActivity{

    Activity context;

    @BindView(R.id.id_set_major_listview)
    ListView listView;
    List<Map<String, String>> datas;
    SimpleAdapter simpleAdapter;

    @BindView(R.id.id_find_major_edittext)
    EditText findMajorEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_major);
        ButterKnife.bind(this);
        context=this;
        checkLocalMajor();
    }

    public void checkLocalMajor(){
        String localMajor=ShareTools.getString(getContext(), ShareConstants.KEY_MAJOR_NAME,"");
        if(!TextUtils.isEmpty(localMajor)){
            ActivityTools.toActivity(this, MainActivity.class);
            finish();
        }else{
            inits();
        }
    }

    private void inits() {
        datas = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(getContext(), datas, R.layout.item_major, new String[]{"major"}, new int[]{R.id.item_major_name});
        listView.setAdapter(simpleAdapter);
        findMajorEditText.addTextChangedListener(textWatcher);
    }

    @OnItemClick(R.id.id_set_major_listview)
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String major=datas.get(i).get("major");
        ShareTools.putString(getContext(), ShareConstants.KEY_MAJOR_NAME,major);
        ActivityTools.toActivity(getContext(),MainActivity.class);
        finish();
    }

    public Activity getContext() {
        return context;
    }

    Callback<ListResult<MajorModel>> findMajorCallback = new Callback<ListResult<MajorModel>>() {
        @Override
        public void onResponse(Call<ListResult<MajorModel>> call, Response<ListResult<MajorModel>> response) {
            ListResult<MajorModel> result = response.body();
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
            TimetableRequest.findMajor(this, key, findMajorCallback);
        }
    }
}
