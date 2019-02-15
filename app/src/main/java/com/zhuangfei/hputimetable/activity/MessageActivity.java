package com.zhuangfei.hputimetable.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.MainActivity;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.adapter.MessageAdapter;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    @BindView(R.id.id_listview)
    ListView listView;

    MessageAdapter adapter;
    List<MessageModel> list;

    @BindView(R.id.id_loadlayout)
    LinearLayout loadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        list=new ArrayList<>();
        adapter=new MessageAdapter(this,list);
        listView.setAdapter(adapter);
        getMessages();
    }

    public void setLoadLayout(boolean isShow){
        if(isShow){
            loadLayout.setVisibility(View.VISIBLE);
        }else {
            loadLayout.setVisibility(View.GONE);
        }
    }

    public void getMessages(){
        String deviceId= DeviceTools.getDeviceId(this);
        if(deviceId==null) return;
        String school= ShareTools.getString(MessageActivity.this, ShareConstants.STRING_SCHOOL_NAME,"unknow");
        setLoadLayout(true);
        TimetableRequest.getMessages(this, deviceId,school,null, new Callback<ListResult<MessageModel>>() {
            @Override
            public void onResponse(Call<ListResult<MessageModel>> call, Response<ListResult<MessageModel>> response) {
                setLoadLayout(false);
                if(response==null) return;
                ListResult<MessageModel> result=response.body();
                if(result.getCode()==200){
                    showMessages(result.getData());
                }else {
                    Toast.makeText(MessageActivity.this,result.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<MessageModel>> call, Throwable t) {
                setLoadLayout(false);
            }
        });
    }

    private void showMessages(List<MessageModel> models) {
        if(models==null) return;
        list.clear();
        list.addAll(models);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.id_back)
    public void goBack(){
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
