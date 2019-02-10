package com.zhuangfei.hputimetable.activity.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.MessageActivity;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.activity.WebViewActivity;
import com.zhuangfei.hputimetable.activity.hpu.HpuRepertoryActivity;
import com.zhuangfei.hputimetable.activity.hpu.ImportMajorActivity;
import com.zhuangfei.hputimetable.adapter.SearchSchoolAdapter;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.SearchResultModel;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.Collections;
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
    List<SearchResultModel> models;
    List<SearchResultModel> allDatas;
    SearchSchoolAdapter searchAdapter;

    @BindView(R.id.id_search_edittext)
    EditText searchEditText;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_school);
        ButterKnife.bind(this);
        inits();
    }

    public void setLoadLayout(boolean isShow) {
        if (isShow) {
            loadLayout.setVisibility(View.VISIBLE);
        } else {
            loadLayout.setVisibility(View.GONE);
        }
    }

    public void toAdapter() {
        ActivityTools.toActivity(this, AdapterTipActivity.class);
    }

    private void inits() {
        context = this;
//        backLayout = findViewById(R.id.id_back);
//        backLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goBack();
//            }
//        });

        models = new ArrayList<>();
        allDatas=new ArrayList<>();
        searchAdapter = new SearchSchoolAdapter(this, allDatas,models);
        searchListView.setAdapter(searchAdapter);
        searchEditText.addTextChangedListener(textWatcher);


        String school= ShareTools.getString(SearchSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME,"unknow");
        search(school);
    }

    @OnItemClick(R.id.id_search_listview)
    public void onItemClick(int i) {
        SearchResultModel model=models.get(i);
        if(model.getType()==SearchResultModel.TYPE_SCHOOL){
            School school = (School) model.getObject();
            ActivityTools.toActivity(this, AdapterSchoolActivity.class,
                    new BundleModel().setFromClass(SearchSchoolActivity.class)
                            .put("school", school.getSchoolName())
                            .put("url", school.getUrl())
                            .put("type", school.getType())
                            .put("parsejs", school.getParsejs()));
        }else {
            StationModel stationModel= (StationModel) model.getObject();
            StationManager.openStationWithout(this,
                    stationModel.getUrl(),
                    stationModel.getName());
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
                models.clear();
                allDatas.clear();
                searchAdapter.notifyDataSetChanged();
            } else {
                search(charSequence.toString());

                if (key.equals("123ZFMAN")) {
                    StationManager.openStationWithout(SearchSchoolActivity.this,
                            "http://www.liuzhuangfei.com/apis/area/station/hpu_import/index.html",
                            "班级课表");
                }
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
        if(TextUtils.isEmpty(key)) {
            return;
        }

        models.clear();
        allDatas.clear();
        searchStation(key);

        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getAdapterSchools(this, key, new Callback<ListResult<School>>() {
                @Override
                public void onResponse(Call<ListResult<School>> call, Response<ListResult<School>> response) {
                    ListResult<School> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showResult(result.getData());
                        } else {
                            Toast.makeText(SearchSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchSchoolActivity.this, "school response is null!", Toast.LENGTH_SHORT).show();
                    }
                    setLoadLayout(false);
                }

                @Override
                public void onFailure(Call<ListResult<School>> call, Throwable t) {
                    Toast.makeText(SearchSchoolActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoadLayout(false);
                }
            });
        }
    }

    public void searchStation(String key) {
        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getStations(this, key, new Callback<ListResult<StationModel>>() {
                @Override
                public void onResponse(Call<ListResult<StationModel>> call, Response<ListResult<StationModel>> response) {
                    setLoadLayout(false);
                    ListResult<StationModel> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showStationResult(result.getData());
                        } else {
                            Toast.makeText(SearchSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchSchoolActivity.this, "school response is null!", Toast.LENGTH_SHORT).show();
                    }
                    setLoadLayout(false);
                }

                @Override
                public void onFailure(Call<ListResult<StationModel>> call, Throwable t) {
                    setLoadLayout(false);
                }
            });
        }
    }

    private void showStationResult(List<StationModel> result) {
        if (result == null) return;
        result.addAll(result);
        result.addAll(result);
        result.addAll(result);
        List<SearchResultModel> addList=new ArrayList<>();
        for (int i=0;i<Math.min(result.size(),SearchSchoolAdapter.TYPE_STATION_MAX_SIZE);i++) {
            StationModel model=result.get(i);
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_STATION);
            if(result.size()>3){
                searchResultModel.setType(SearchResultModel.TYPE_STATION_MORE);
            }
            searchResultModel.setObject(model);
            addModelToList(searchResultModel);
        }

        for (int i=0;i<result.size();i++) {
            StationModel model=result.get(i);
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_STATION);
            searchResultModel.setObject(model);
            addList.add(searchResultModel);
        }

        sortResult();
        addAllDataToList(addList);
        sortResultForAllDatas();
        searchAdapter.notifyDataSetChanged();
    }

    private void showResult(List<School> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        for (School schoolBean : list) {
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_SCHOOL);
            searchResultModel.setObject(schoolBean);
            addModelToList(searchResultModel);
        }
        sortResult();
        addAllDataToList(models);
        sortResultForAllDatas();
        searchAdapter.notifyDataSetChanged();
    }

    public void sortResult() {
        if (models != null) {
            Collections.sort(models);
        }
    }

    public void sortResultForAllDatas() {
        if (allDatas != null) {
            Collections.sort(allDatas);
        }
    }

    public synchronized void addModelToList(SearchResultModel searchResultModel) {
        if (models != null) {
            models.add(searchResultModel);
        }
    }

    public synchronized void addAllDataToList(List<SearchResultModel> searchResultModels) {
        if (allDatas != null) {
            for(SearchResultModel model:searchResultModels){
                allDatas.add(model);
            }
        }
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.id_menu_search)
    public void toSearchActivity() {
        ActivityTools.toActivity(this, HpuRepertoryActivity.class);
    }

    @OnClick(R.id.id_menu_changeclass)
    public void changeClass() {
        ActivityTools.toActivity(this, ImportMajorActivity.class);
    }

    @OnClick(R.id.id_menu_food)
    public void food() {
        Toasty.info(this, "暂未开放!").show();
    }

    @OnClick(R.id.id_menu_score)
    public void score() {
        int show = ShareTools.getInt(this, ShareConstants.KEY_SHOW_ALERTDIALOG, 1);
        if (show == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("查询指南")
                    .setMessage("步骤如下：\n\n1.点击[确认]\n2.登录VPN,若失败,可以使用其他同学的校园网账号,vpn密码默认是身份证后六位" +
                            "\n3.登陆教务处,输入个人教务处账号,密码默认为学号\n4.登陆成功后,网页无法点击,这是正常现象." +
                            "\n4.此时,点击右上角,选择[兼容模式菜单],选择需要的功能即可\n");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BundleModel model = new BundleModel();
                    model.setFromClass(MainActivity.class);
                    model.put("title", "成绩查询");
                    model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
                    ShareTools.putInt(SearchSchoolActivity.this, ShareConstants.KEY_SHOW_ALERTDIALOG, 0);
                    ActivityTools.toActivity(SearchSchoolActivity.this, WebViewActivity.class, model);
                }
            }).setNegativeButton("取消", null);
            builder.create().show();
        } else {
            BundleModel model = new BundleModel();
            model.setFromClass(MainActivity.class);
            model.put("title", "成绩查询");
            model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
            ActivityTools.toActivity(this, WebViewActivity.class, model);
        }
    }
}
