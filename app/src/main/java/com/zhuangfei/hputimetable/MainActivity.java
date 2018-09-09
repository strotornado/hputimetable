package com.zhuangfei.hputimetable;

import java.util.ArrayList;
import java.util.List;

import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.fragment.FuncFragment;
import com.zhuangfei.hputimetable.adapter.MyFragmentPagerAdapter;
import com.zhuangfei.hputimetable.fragment.ScheduleFragment;
import com.zhuangfei.hputimetable.listener.OnSwitchTableListener;
import com.zhuangfei.hputimetable.listener.OnTitleClickedListener;
import com.zhuangfei.hputimetable.listener.OnStatusChangedListener;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import es.dmoral.toasty.Toasty;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends AppCompatActivity implements OnStatusChangedListener{

    private static final String TAG = "MainActivity";

    final int SUCCESSCODE = 1;

    ViewPager mViewPager;
    List<Fragment> mFragmentList;
    MyFragmentPagerAdapter mAdapter;

    OnTitleClickedListener onTitleClickedListener;
    OnSwitchTableListener onSwitchTableListener;

    @BindView(R.id.id_specialtitle)
    public TextView specialText;

    @BindView(R.id.id_layout)
    public LinearLayout scheduleTitleLayout;

    @BindView(R.id.id_title)
    public TextView mTitleTextView;

    @BindView(R.id.id_schedulename)
    public TextView mCurScheduleTextView;

    @BindView(R.id.id_main_menu)
    ImageView menuImageView;

    public static final int REQUEST_IMPORT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        inits();
        shouldcheckPermission();
    }

    private void inits() {
        menuImageView.setColorFilter(Color.GRAY);
        mViewPager=findViewById(R.id.id_viewpager);

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FuncFragment());
        mFragmentList.add(new ScheduleFragment());
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                scheduleTitleLayout.setVisibility(View.GONE);
                specialText.setVisibility(View.GONE);
                switch (position){
                    case 0:
                        specialText.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        scheduleTitleLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int item= BundleTools.getToItem(this,1);
        select(item);
    }

    public void select(int i){
        scheduleTitleLayout.setVisibility(View.GONE);
        specialText.setVisibility(View.GONE);
        if(i>1) i=1;
        switch (i){
            case 0:
                specialText.setVisibility(View.VISIBLE);
                mViewPager.setCurrentItem(0);
                break;
            case 1:
                scheduleTitleLayout.setVisibility(View.VISIBLE);
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    @OnClick(R.id.id_main_menu)
    public void showPopMenu(){
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(this, menuImageView);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.id_menu1:
                        ActivityTools.toActivity(MainActivity.this,MultiScheduleActivity.class);
                        finish();
                        break;
                    case R.id.id_menu2:
                        ActivityTools.toActivity(MainActivity.this,AddTimetableActivity.class);
                        finish();
                        break;
                    case R.id.id_menu3:
                        ActivityTools.toActivity(MainActivity.this,ScanActivity.class);
                        finish();
                        break;
                    case R.id.id_menu4:
                        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                        intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_IMPORT);
                        startActivityForResult(intent, REQUEST_IMPORT);
                        break;
                    case R.id.id_menu5:
                        ActivityTools.toActivity(MainActivity.this,MenuActivity.class);
                        finish();
                        break;
                }
                return false;
            }
        });
        popup.show(); //这一行代码不要忘记了
    }

    /**
     * 接收授权页面获取的课程信息
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == AuthActivity.RESULT_STATUS) {
            SuperResult result= SuperUtils.getResult(data);
            if(result==null){
                Toasty.error(MainActivity.this, "result is null").show();
            }else{
                if(result.isSuccess()){
                    List<SuperLesson> lessons = result.getLessons();
                    ScheduleName newName = ScheduleDao.saveSuperShareLessons(lessons);
                    if (newName != null) {
                        showDialogOnApply(newName);
//                        ActivityTools.toActivity(this, MultiScheduleActivity.class);
//                        finish();
                    } else {
                        Toasty.error(MainActivity.this, "ScheduleName is null").show();
                    }
                }else{
                    Toasty.error(MainActivity.this, ""+result.getErrMsg()).show();
                }
            }
        }
    }

    private void showDialogOnApply(final ScheduleName name) {
        if(name==null) return;
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("你导入的数据已存储在多课表["+name.getName()+"]下!\n是否直接设置为当前课表?")
                .setTitle("课表导入成功")
                .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(onSwitchTableListener!=null){
                            onSwitchTableListener.onSwitchTable(name);
                        }
                        BroadcastUtils.refreshAppWidget(MainActivity.this);
                        if(dialogInterface!=null){
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("稍后设置",null);
        builder.create().show();
    }

    private void shouldcheckPermission() {
        PermissionGen.with(MainActivity.this)
                .addRequestCode(SUCCESSCODE)
                .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.VIBRATE
                )
                .request();
    }

    //申请权限结果的返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //权限申请成功
    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        //在这个方法中做一些权限申请成功的事情

    }

    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        ToastTools.show(this, "权限不足，运行中可能会出现故障!");
    }

    @Override
    public void onWeekChanged(int cur) {
        mTitleTextView.setText("第"+cur+"周");
    }

    @Override
    public void onScheduleNameChanged(String scheduleName) {
        mCurScheduleTextView.setText(scheduleName);
    }

    @OnClick(R.id.id_layout)
    public void onTitleClick(){
        if(onTitleClickedListener!=null){
            onTitleClickedListener.onTitleClick();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragment instanceof OnTitleClickedListener){
            onTitleClickedListener= (OnTitleClickedListener) fragment;
        }
        if(fragment instanceof OnSwitchTableListener){
            onSwitchTableListener= (OnSwitchTableListener) fragment;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onTitleClickedListener=null;
    }
}
