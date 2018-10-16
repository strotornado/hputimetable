package com.zhuangfei.hputimetable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import org.litepal.crud.DataSupport;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class AdapterSameTypeActivity extends AppCompatActivity {

    public static final String STRING_EXTRA_NAME = "extra_name";
    public static final String INT_EXTRA_ID = "extra_id";

    @BindView(R.id.id_school_edittext)
    EditText nameEdit;

    @BindView(R.id.id_url_edittext)
    EditText urlEdit;

    @BindView(R.id.id_title)
    TextView titleTextView;

    String js=null;
    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_same_type);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        js = BundleTools.getString(this,"js", null);
        String type = BundleTools.getString(this,"type", null);
        if(TextUtils.isEmpty(js)||TextUtils.isEmpty(type)){
            Toasty.error(this,"js或者教务类型未知，结果不可预期！").show();
            goBack();
        }else{
            titleTextView.setText(type+"教务系统");
        }
    }

    @OnClick(R.id.cv_other)
    public void save() {
        String name = nameEdit.getText().toString();
        String url = urlEdit.getText().toString();
        if (TextUtils.isEmpty(name)||TextUtils.isEmpty(url)) {
            Toasty.warning(this, "不可为空", Toast.LENGTH_SHORT).show();
        } else {
            ActivityTools.toActivity(this,AdapterSchoolActivity.class,
                    new BundleModel().setFromClass(MainActivity.class)
                            .put("school",name)
                            .put("url",url)
                            .put("parsejs",js));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @OnClick(R.id.id_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }
}
