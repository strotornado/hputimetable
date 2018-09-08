package com.zhuangfei.hputimetable;

import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
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

public class ModifyScheduleNameActivity extends AppCompatActivity {

    public static final String STRING_EXTRA_NAME = "extra_name";
    public static final String INT_EXTRA_ID = "extra_id";

    @BindView(R.id.et_schedulename)
    EditText nameEdit;

    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_schedule_name);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        String name = BundleTools.getString(this, STRING_EXTRA_NAME, null);
        id = (int) BundleTools.getInt(this, INT_EXTRA_ID, -1);
        if (name == null || id == -1) {
            goBack();
        } else {
            nameEdit.setText(name);
        }
    }

    @OnClick(R.id.cv_save)
    public void save() {
        String name = nameEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toasty.warning(this, "课表名称不可为空", Toast.LENGTH_SHORT).show();
        } else if (name.equals("默认课表")) {
            Toasty.error(this, "名称不合法").show();
        } else {
            ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
            if (scheduleName != null) {
                scheduleName.setName(name);
                scheduleName.update(scheduleName.getId());
                Toasty.success(this, "修课表成功", Toast.LENGTH_SHORT).show();
                goBack();
            } else {
                Toasty.error(this, "修改课表失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @OnClick(R.id.id_back)
    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class,
                new BundleModel().setToItem(2));
    }
}
