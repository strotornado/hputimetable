package com.zhuangfei.hputimetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.AdapterDebugModel;
import com.zhuangfei.hputimetable.api.model.HtmlSummary;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.UserDebugModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

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

public class AdapterDebugHtmlActivity extends AppCompatActivity {

    @BindView(R.id.id_listView)
    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String,String>> list=new ArrayList<>();

    String aid="";

    @BindView(R.id.id_debug_html_title)
    TextView titleTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_debug_html);
        ButterKnife.bind(this);
        String schoolName=getIntent().getStringExtra("schoolName");
        aid=getIntent().getStringExtra("aid");

        simpleAdapter=new SimpleAdapter(this,list,R.layout.item_adapter_debug,
                new String[]{"name"},
                new int[]{R.id.tv_name});
        listView.setAdapter(simpleAdapter);

        titleTextView.setText(schoolName);
        getData(schoolName);
    }

    public void getData(String schoolName) {
        TimetableRequest.findHtmlSummary(this, schoolName, new Callback<ListResult<HtmlSummary>>() {
            @Override
            public void onResponse(Call<ListResult<HtmlSummary>> call, Response<ListResult<HtmlSummary>> response) {
                ListResult<HtmlSummary> result=response.body();
                if(result!=null){
                    List<HtmlSummary> summaryList=result.getData();
                    if(result.getCode()==200){
                        showList(summaryList);
                    }else{
                        Toasty.error(AdapterDebugHtmlActivity.this,result.getMsg()).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ListResult<HtmlSummary>> call, Throwable t) {
                Toasty.error(AdapterDebugHtmlActivity.this,t.getMessage()).show();
            }
        });
    }

    private void showList(List<HtmlSummary> htmlSummary) {
        list.clear();
        for(HtmlSummary model:htmlSummary){
            if(model!=null){
                Map<String,String> map=new HashMap<>();
                map.put("name",model.getFilename());
                list.add(map);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    @OnItemClick(R.id.id_listView)
    public void onItemClick(int pos){
        Intent intent=new Intent(this,DebugActivity.class);
        intent.putExtra("uid", ShareTools.getString(this,"debug_userkey",null));
        intent.putExtra("aid",aid);
        intent.putExtra("filename",list.get(pos).get("name"));
        startActivity(intent);
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
       finish();
    }
}
