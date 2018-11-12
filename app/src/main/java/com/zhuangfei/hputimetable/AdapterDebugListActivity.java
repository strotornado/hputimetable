package com.zhuangfei.hputimetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.AdapterDebugModel;
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

public class AdapterDebugListActivity extends AppCompatActivity {

    @BindView(R.id.id_listView)
    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String,String>> list=new ArrayList<>();

    String name;
    String userkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_debug_list);
        ButterKnife.bind(this);

        simpleAdapter=new SimpleAdapter(this,list,R.layout.item_adapter_debug,
                new String[]{"name"},
                new int[]{R.id.tv_name});
        listView.setAdapter(simpleAdapter);

        name= ShareTools.getString(this,"debug_name",null);
        userkey= ShareTools.getString(this,"debug_userkey",null);
        if(name==null||userkey==null){
            ActivityTools.toBackActivityAnim(this,AdapterDebugTipActivity.class);
        }
        getData();
    }

    public void getData() {
        TimetableRequest.getUserInfo(this, name, userkey,
                new Callback<ObjResult<UserDebugModel>>() {
                    @Override
                    public void onResponse(Call<ObjResult<UserDebugModel>> call, Response<ObjResult<UserDebugModel>> response) {
                        ObjResult<UserDebugModel> result=response.body();
                        if(result!=null){
                            UserDebugModel modle=result.getData();
                            if(result.getCode()==200){
                                List<AdapterDebugModel> list=modle.getMyAdapter();
                                showList(list);
                            }else{
                                Toasty.error(AdapterDebugListActivity.this,result.getMsg()).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ObjResult<UserDebugModel>> call, Throwable t) {
                        Toasty.error(AdapterDebugListActivity.this,t.getMessage()).show();
                    }
                });
    }

    private void showList(List<AdapterDebugModel> data) {
       list.clear();
       int index=1;
       for(AdapterDebugModel model:data){
           if(model!=null){
               Map<String,String> map=new HashMap<>();
               map.put("name",model.getSchoolName());
               map.put("aid",model.getAid()+"");
               list.add(map);
           }
           index++;
       }
       simpleAdapter.notifyDataSetChanged();
    }

    @OnItemClick(R.id.id_listView)
    public void onItemClick(int pos){
        Intent intent=new Intent(this,AdapterDebugHtmlActivity.class);
        intent.putExtra("schoolName",list.get(pos).get("name"));
        intent.putExtra("aid",list.get(pos).get("aid"));
        startActivity(intent);
    }

    @OnClick(R.id.ib_back)
    public void goBack(){
        finish();
    }

    @OnClick(R.id.tv_logout)
    public void logout(){
        clearLocal();
        ActivityTools.toBackActivityAnim(this,AdapterDebugTipActivity.class);
    }

    void clearLocal(){
        ShareTools.putString(this,"debug_name",null);
        ShareTools.putString(this,"debug_userkey",null);
    }
}
