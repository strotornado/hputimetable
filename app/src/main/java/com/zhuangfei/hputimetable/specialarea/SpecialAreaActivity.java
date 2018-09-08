package com.zhuangfei.hputimetable.specialarea;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecialAreaActivity extends AppCompatActivity {

    @BindView(R.id.id_area_navlayout)
    LinearLayout navLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_area);
        ButterKnife.bind(this);
        getData();
    }

    public void getData() {
        List<AreaNav> navs=new ArrayList<>();
        AreaNav nav1=new AreaNav();
        AreaNav nav2=new AreaNav();
        AreaNav nav3=new AreaNav();

        nav1.setText("学习");
        nav1.setType(AreaNav.TYPE_CLICK);

        nav2.setText("考试");
        nav2.setType(AreaNav.TYPE_CLICK);

        nav3.setText("休闲");
        nav3.setType(AreaNav.TYPE_CLICK);

        navs.add(nav1);
        navs.add(nav2);
        navs.add(nav3);

        renderView(navs);
    }

    private void renderView(List<AreaNav> navs) {
        LayoutInflater inflater=LayoutInflater.from(this);
        for(AreaNav nav:navs){
            View v=inflater.inflate(R.layout.item_area_bottomnav,null,false);
            TextView textView=v.findViewById(R.id.item_area_text);
            textView.setText(nav.getText());
            navLayout.addView(v);
        }
    }
}
