package com.zhuangfei.hputimetable.activity.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.LoginActivity;
import com.zhuangfei.hputimetable.activity.MessageActivity;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.activity.WebViewActivity;
import com.zhuangfei.hputimetable.activity.hpu.HpuRepertoryActivity;
import com.zhuangfei.hputimetable.activity.hpu.ImportMajorActivity;
import com.zhuangfei.hputimetable.adapter.SearchSchoolAdapter;
import com.zhuangfei.hputimetable.adapter_apis.AssetTools;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.AdapterResultV2;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.api.model.TemplateModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.model.GreenFruitSchool;
import com.zhuangfei.hputimetable.model.SearchResultModel;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.hputimetable.tools.ThemeManager;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

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
    List<TemplateModel> templateModels;
    String baseJs;

    @BindView(R.id.id_search_edittext)
    EditText searchEditText;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    boolean firstStatus=true;

    List<GreenFruitSchool> allSchool;

    @BindView(R.id.id_layout_hpusa)
    LinearLayout hpuLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_school);
        ViewTools.setStatusTextGrayColor(this);
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

    private void inits() {
        context = this;
//        backLayout = findViewById(R.id.id_back);
//        backLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goBack();
//            }
//        });

        allSchool=new ArrayList<>();
        models = new ArrayList<>();
        allDatas=new ArrayList<>();
        searchAdapter = new SearchSchoolAdapter(this, allDatas,models);
        searchListView.setAdapter(searchAdapter);
        searchEditText.addTextChangedListener(textWatcher);

        loadSchools();
        String school= ShareTools.getString(SearchSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME,"unknow");
        search(school);
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
                allSchool= (List<GreenFruitSchool>) msg.obj;
            }
        }
    };

    @OnItemClick(R.id.id_search_listview)
    public void onItemClick(int i) {
        SearchResultModel model=models.get(i);
        if(model==null) return;
        //通用算法解析
        if(model.getType()==SearchResultModel.TYPE_COMMON){
            TemplateModel templateModel = (TemplateModel) model.getObject();
            if (templateModel!=null){
                if(baseJs==null){
                    ToastTools.show(this,"基础函数库发生异常，请联系qq:1193600556");
                }else if(templateModel.getTemplateTag().startsWith("custom/")){
                    ActivityTools.toActivityWithout(this, AdapterTipActivity.class);
                }
                else {
                    ActivityTools.toActivityWithout(this,
                            AdapterSameTypeActivity.class, new BundleModel()
                                    .put("type", templateModel.getTemplateName())
                                    .put("js", templateModel.getTemplateJs()+baseJs));
                }
            }
        }
        //学校教务导入
        else if(model.getType()==SearchResultModel.TYPE_SCHOOL){
            School school = (School) model.getObject();
            if(school!=null){
                if(school.getParsejs()!=null&&school.getParsejs().startsWith("template/")){
                    TemplateModel searchModel=searchInTemplate(templateModels,school.getParsejs());
                    if(baseJs==null){
                        ToastTools.show(this,"基础函数库发生异常，请联系qq:1193600556");
                        return;
                    }
                    if(searchModel!=null){
                        ActivityTools.toActivityWithout(this, AdapterSchoolActivity.class,
                                new BundleModel().setFromClass(SearchSchoolActivity.class)
                                        .put("school", school.getSchoolName())
                                        .put("url", school.getUrl())
                                        .put("type", school.getType())
                                        .put("parsejs",searchModel.getTemplateJs()+baseJs));
                    }else {
                        ToastTools.show(this,"通用解析模板发生异常，请联系qq:1193600556");
                    }
                }else{
                    ActivityTools.toActivityWithout(this, AdapterSchoolActivity.class,
                            new BundleModel().setFromClass(SearchSchoolActivity.class)
                                    .put("school", school.getSchoolName())
                                    .put("url", school.getUrl())
                                    .put("type", school.getType())
                                    .put("parsejs",school.getParsejs()));
                }

            }
        }
        else if(model.getType()==SearchResultModel.TYPE_XIQUER){
            GreenFruitSchool school = (GreenFruitSchool) model.getObject();
            ActivityTools.toActivityWithout(this, LoginActivity.class,
                    new BundleModel()
            .put("selectSchool",school));
        }
        //服务站
        else{
            StationModel stationModel= (StationModel) model.getObject();
            StationManager.openStationWithout(this,stationModel);
        }
    }

    public TemplateModel searchInTemplate(List<TemplateModel> models,String tag){
        if(models==null||tag==null) return null;
        for(TemplateModel model:models){
            if(model!=null){
                if(tag.equals(model.getTemplateTag())){
                    return model;
                }
            }
        }
        return null;
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
            firstStatus=false;
            if (TextUtils.isEmpty(key)) {
                hpuLayout.setVisibility(View.GONE);
                models.clear();
                allDatas.clear();
                searchAdapter.notifyDataSetChanged();
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

    public void search(final String key) {
        if(TextUtils.isEmpty(key)) {
            hpuLayout.setVisibility(View.GONE);
            return;
        }
        if(key.indexOf("河南理工")!=-1){
            hpuLayout.setVisibility(View.VISIBLE);
        }else{
            hpuLayout.setVisibility(View.GONE);
        }

        models.clear();
        allDatas.clear();
        searchStation(key);

        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getAdapterSchoolsV2(this, key, new Callback<ObjResult<AdapterResultV2>>() {
                @Override
                public void onResponse(Call<ObjResult<AdapterResultV2>> call, Response<ObjResult<AdapterResultV2>> response) {
                    ObjResult<AdapterResultV2> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showResult(result.getData(),key);
                        } else {
                            Toast.makeText(SearchSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchSchoolActivity.this, "school response is null!", Toast.LENGTH_SHORT).show();
                    }
                    setLoadLayout(false);
                }

                @Override
                public void onFailure(Call<ObjResult<AdapterResultV2>> call, Throwable t) {
                    setLoadLayout(false);
                }
            });
        }
    }

    public void searchStation(final String key) {
        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getStations(this, key, new Callback<ListResult<StationModel>>() {
                @Override
                public void onResponse(Call<ListResult<StationModel>> call, Response<ListResult<StationModel>> response) {
                    setLoadLayout(false);
                    ListResult<StationModel> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showStationResult(result.getData(),key);
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

    private void showStationResult(List<StationModel> result,String key) {
        if(!firstStatus&&searchEditText.getText()!=null&&key!=null&&!searchEditText.getText().toString().equals(key)){
            return;
        }
        if (result == null) return;
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
        searchAdapter.notifyDataSetChanged();
    }

    /**
     *
     * @param result
     * @param key 用于校验输入框是否发生了变化，如果变化，则忽略
     */
    private void showResult(AdapterResultV2 result,String key) {
        if(!firstStatus&&searchEditText.getText()!=null&&key!=null&&!searchEditText.getText().toString().equals(key)){
            return;
        }
        if(result==null) return;
        baseJs=result.getBase();
        templateModels=result.getTemplate();
        List<School> list=result.getSchoolList();
        if (list == null) {
            return;
        }

        if(templateModels!=null){
            for (TemplateModel model : templateModels) {
                if(firstStatus||(model.getTemplateName()!=null&&model.getTemplateName().indexOf(key)!=-1)){
                    SearchResultModel searchResultModel = new SearchResultModel();
                    searchResultModel.setType(SearchResultModel.TYPE_COMMON);
                    searchResultModel.setObject(model);
                    addModelToList(searchResultModel);
                }
            }
        }

        if(allSchool!=null){
            for (GreenFruitSchool schoolBean : allSchool) {
                if (schoolBean.getXxmc() != null && schoolBean.getXxmc().indexOf(key) != -1) {
                    SearchResultModel searchResultModel = new SearchResultModel();
                    searchResultModel.setType(SearchResultModel.TYPE_XIQUER);
                    searchResultModel.setObject(schoolBean);
                    addModelToList(searchResultModel);
                }
            }
        }

        for (School schoolBean : list) {
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_SCHOOL);
            searchResultModel.setObject(schoolBean);
            addModelToList(searchResultModel);
        }

        SearchResultModel searchResultModel = new SearchResultModel();
        searchResultModel.setType(SearchResultModel.TYPE_COMMON);
        TemplateModel addAdapterModel=new TemplateModel();
        addAdapterModel.setTemplateName("添加学校适配");
        addAdapterModel.setTemplateTag("custom/upload");
        searchResultModel.setObject(addAdapterModel);

        if(firstStatus||addAdapterModel.getTemplateName().indexOf(key)!=-1){
            addModelToList(searchResultModel);
        }
        sortResult();
        searchAdapter.notifyDataSetChanged();
    }

    public void sortResult() {
        if (models != null) {
            Collections.sort(models);
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
