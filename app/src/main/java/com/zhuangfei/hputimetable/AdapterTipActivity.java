package com.zhuangfei.hputimetable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class AdapterTipActivity extends AppCompatActivity {

    @BindView(R.id.id_school_edittext)
    public EditText schoolEdit;

    @BindView(R.id.id_url_edittext)
    public EditText urlEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_tip);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.cv_adapter)
    public void onAdapterBtnClicked() {
        String school = schoolEdit.getText().toString();
        String url = urlEdit.getText().toString();
        if (TextUtils.isEmpty(school) || TextUtils.isEmpty(url)) {
            Toasty.warning(this, "不允许为空，请填充完整!").show();
        } else {
            ActivityTools.toActivity(this, UploadHtmlActivity.class,
                    new BundleModel()
                            .put("url", url)
                            .put("school", school));
        }
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
