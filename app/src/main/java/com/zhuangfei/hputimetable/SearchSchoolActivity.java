package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.model.BundleModel;
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

public class SearchSchoolActivity extends AppCompatActivity {

    Activity context;

    @BindView(R.id.id_search_listview)
    ListView searchListView;
    List<Map<String, String>> data;
    List<School> schools;
    SimpleAdapter adapter;

    @BindView(R.id.id_search_edittext)
    EditText searchEditText;

    LinearLayout backLayout;

    @BindView(R.id.id_layout_hpusa)
    LinearLayout hpuAreaLayout;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    @BindView(R.id.id_tip)
    LinearLayout tipLayout;

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

    @OnClick(R.id.id_goto_adapter)
    public void toAdapter(){
        ActivityTools.toActivity(this, AdapterTipActivity.class);
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
        schools = new ArrayList<>();
        adapter = new SimpleAdapter(this, data, R.layout.item_search_school, new String[]{
                "name"}, new int[]{R.id.item_school_val});
        searchListView.setAdapter(adapter);
        searchEditText.addTextChangedListener(textWatcher);
    }

    @OnItemClick(R.id.id_search_listview)
    public void onItemClick(int i) {
        School school = schools.get(i);
        ActivityTools.toActivity(this, AdapterSchoolActivity.class,
                new BundleModel().setFromClass(SearchSchoolActivity.class)
                        .put("school", school.getSchoolName())
                        .put("url", school.getUrl())
                        .put("type", school.getType())
                        .put("parsejs", school.getParsejs()));
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
                adapter.notifyDataSetChanged();
                hpuAreaLayout.setVisibility(View.GONE);
                tipLayout.setVisibility(View.VISIBLE);
            } else {
                tipLayout.setVisibility(View.GONE);
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
        if (TextUtils.isEmpty(key) || key.indexOf("河南理工") == -1) {
            hpuAreaLayout.setVisibility(View.GONE);
        } else {
            hpuAreaLayout.setVisibility(View.VISIBLE);
        }
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

    private void showResult(List<School> list) {
        if (list == null||list.size()==0) {
            tipLayout.setVisibility(View.VISIBLE);
            return;
        }
        tipLayout.setVisibility(View.GONE);

        data.clear();
        schools.clear();
        schools.addAll(list);
        for (School school : list) {
            if (school != null) {
                Map<String, String> map = new HashMap<>();
                String type = school.getType();
                if (TextUtils.isEmpty(type)) {
                    map.put("name", school.getSchoolName());
                }else{
                    map.put("name", school.getSchoolName() + "-" + type );
                }
                data.add(map);
            }
        }
        adapter.notifyDataSetChanged();
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
        Toasty.info(this,"暂未开放!").show();
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
