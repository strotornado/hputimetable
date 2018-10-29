package com.zhuangfei.hputimetable;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.CheckModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterTipActivity extends AppCompatActivity {

    @BindView(R.id.id_school_edittext)
    public EditText schoolEdit;

    @BindView(R.id.id_url_edittext)
    public EditText urlEdit;

    @BindView(R.id.cv_adapter)
    CardView adapterCard;

    @BindView(R.id.cv_check)
    CardView checkCard;

    @BindView(R.id.tv_name)
    TextView nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_tip);
        ButterKnife.bind(this);
        schoolEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence!=null&&charSequence.length()>=3){
                    check(charSequence.toString());
                }else{
                    checkCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.cv_adapter)
    public void onAdapterBtnClicked() {
        final String school = schoolEdit.getText().toString();
        final String url = urlEdit.getText().toString();
        if (TextUtils.isEmpty(school) || TextUtils.isEmpty(url)) {
            Toasty.warning(this, "不允许为空，请填充完整!").show();
        } else if(!url.startsWith("http://")&&!url.startsWith("https://")){
            Toasty.warning(this, "请填写正确的url，以http://或https://开头").show();
            return;
        }else{
            if (!school.endsWith("学校")&&!school.endsWith("学院")&&school.endsWith("大学")) {
                Toasty.warning(this, "").show();
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(this)
                    .setTitle("校名不太对哟")
                    .setMessage("你的校名好像不太对哟，务必填写全称")
                    .setPositiveButton("确定是对的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityTools.toActivity(AdapterTipActivity.this, UploadHtmlActivity.class,
                                    new BundleModel()
                                            .put("url", url)
                                            .put("school", school));
                        }
                    })
                    .setNegativeButton("取消",null);
            builder.create().show();
        }
    }

    public void check(String school) {
        TimetableRequest.checkSchool(this, school, new Callback<ObjResult<CheckModel>>() {
            @Override
            public void onResponse(Call<ObjResult<CheckModel>> call, Response<ObjResult<CheckModel>> response) {
                ObjResult<CheckModel> result=response.body();
                if(result==null){
                    Toasty.error(AdapterTipActivity.this,"result is null").show();
                }else if(result.getCode()!=200){
                    Toasty.error(AdapterTipActivity.this,result.getMsg()).show();
                }else{
                    CheckModel model=result.getData();
                    if(model!=null){
                        if(model.getHave()==1&&!TextUtils.isEmpty(model.getUrl())&&!TextUtils.isEmpty(model.getName())){
                            urlEdit.setText(model.getUrl()==null?"":model.getUrl());
                            checkCard.setVisibility(View.VISIBLE);
                            nameText.setText(model.getName());
                        }else{
                            checkCard.setVisibility(View.GONE);
                            urlEdit.setText("");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ObjResult<CheckModel>> call, Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.ib_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }
}
