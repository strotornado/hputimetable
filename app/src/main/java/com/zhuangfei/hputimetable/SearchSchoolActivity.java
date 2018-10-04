package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.School;
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
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchSchoolActivity extends AppCompatActivity {

    Activity context;

    @BindView(R.id.id_search_listview)
    ListView searchListView;
    List<String> data;
    List<School> schools;
    ArrayAdapter arrayAdapter;

    @BindView(R.id.id_search_edittext)
    EditText searchEditText;

    LinearLayout backLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_school);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        context = this;
        backLayout = findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        data = new ArrayList<>();
        schools=new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2,data);
        searchListView.setAdapter(arrayAdapter);
        searchEditText.addTextChangedListener(textWatcher);
    }

    @OnItemClick(R.id.id_search_listview)
    public void onItemClick(int i) {
        School school=schools.get(i);
        if(school!=null){
            Toast.makeText(this,school.getSchoolName()+","+school.getParsejs(),Toast.LENGTH_SHORT).show();
        }
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
            String key = charSequence.toString();
            if (TextUtils.isEmpty(key)) {
                data.clear();
                schools.clear();
                arrayAdapter.notifyDataSetChanged();
            } else {
                search(charSequence.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @OnClick(R.id.id_search_search)
    public void search() {
        String key = searchEditText.getText().toString();
        search(key);
    }

    public void search(String key) {
        if (!TextUtils.isEmpty(key)) {
            TimetableRequest.getAdapterSchools(this, key,new Callback<ListResult<School>>() {
                @Override
                public void onResponse(Call<ListResult<School>> call, Response<ListResult<School>> response) {
                    ListResult<School> result=response.body();
                    if(result!=null){
                        if(result.getCode()==200){
                            showResult(result.getData());
                        }else{
                            Toast.makeText(SearchSchoolActivity.this,result.getMsg(),Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(SearchSchoolActivity.this,"school response is null!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ListResult<School>> call, Throwable t) {
                    Toast.makeText(SearchSchoolActivity.this, t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showResult(List<School> list) {
        if(list==null) return;
        data.clear();
        schools.clear();
        schools.addAll(list);
        for(School school:list){
            if(school!=null){
                data.add(school.getSchoolName());
            }
        }
        arrayAdapter.notifyDataSetChanged();
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
