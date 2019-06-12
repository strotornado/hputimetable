package com.zhuangfei.hputimetable.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.GreenFruitCourse;
import com.zhuangfei.hputimetable.api.model.GreenFruitProfile;
import com.zhuangfei.hputimetable.api.model.GreenFruitTerm;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.event.SelectSchoolEvent;
import com.zhuangfei.hputimetable.event.UpdateScheduleEvent;
import com.zhuangfei.hputimetable.model.GreenFruitSchool;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.ImportTools;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.ViewTools;
import com.zhuangfei.qingguo.GreenFruit;
import com.zhuangfei.qingguo.ParamsManager;
import com.zhuangfei.qingguo.utils.GreenFruitParams;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;

    EditText userName;
    EditText userPassword;

    @BindView(R.id.user_select_school)
    TextView userSchool;

    LinearLayout loadingLayout;

    @BindView(R.id.id_termlayout)
    LinearLayout termLayout;

    GreenFruitSchool selectSchool=null;

    @BindView(R.id.id_term_listview)
    ListView listView;
    List<String> list;
    GreenFruitTerm termList;
    ArrayAdapter<String> adapter;

    GreenFruitProfile greenFruitProfile;

    @BindView(R.id.id_term_title)
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewTools.setTransparent(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
        initEvent();

//        GreenFruitParams params=ParamsManager.get(this).getTermParams("b1591547544272865369","12597_201802010742",
//                "STU","12597","378976");
//        String s=ParamsManager.get(this).map2String(params.getMap());
//        userName.setText(s);
    }

    private void initEvent() {
        loginButton.setOnClickListener(this);
    }

    private void initView() {
        Object obj=BundleTools.getObject(this,"selectSchool",null);
        if(obj!=null&&obj instanceof GreenFruitSchool){
            selectSchool= (GreenFruitSchool) obj;
            userSchool.setText(selectSchool.getXxmc());
        }

        loginButton = (Button) findViewById(R.id.login);
        userName = (EditText) findViewById(R.id.user_name);
        userPassword = (EditText) findViewById(R.id.user_password);
        loadingLayout=findViewById(R.id.id_loadlayout);

        list=new ArrayList<>();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                termLayout.setVisibility(View.GONE);
                GreenFruitTerm.XnxqBean term=termList.getXnxq().get(i);
                getCourses(term);
            }
        });
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.login:
                login();
                break;
            default:
                break;
        }
    }

    public Context getContext(){
        return this;
    }

    /**
     * 登录请求服务器
     */
    private void login() {
        try{
            View view = getWindow().peekDecorView();
            if (view != null) {
                InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }catch (Exception e){}

        final String name = userName.getText().toString();
        final String pw = userPassword.getText().toString();
        if (selectSchool==null) {
            ToastTools.show(this, "请选择学校");
            return;
        }
        if (name.isEmpty() || pw.isEmpty()) {
            ToastTools.show(this, "不可以为空");
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);
        ShareTools.putString(LoginActivity.this, "username", name);
        ShareTools.putString(LoginActivity.this, "password", pw);
        login(name,pw);
    }

    public void login(String name,String pw){
        TimetableRequest.loginGreenFruit(getContext(), selectSchool.getXxdm(),
                name, pw, new Callback<GreenFruitProfile>() {
                    @Override
                    public void onResponse(Call<GreenFruitProfile> call, Response<GreenFruitProfile> response) {
                        loadingLayout.setVisibility(View.GONE);
                        GreenFruitProfile profile=response.body();
                        if(profile!=null&&profile.getFlag()!=null){
                            if(profile.getFlag().equals("0")){
                                greenFruitProfile=profile;
                                titleText.setText(profile.getXm()+"-请选择学期");
                                getTerm(profile);
                            }else{
                                ToastTools.show(getContext(),"Error:["+profile.getFlag()+"] "+profile.getMsg());
                            }
                        }else {
                            ToastTools.show(getContext(),"Errot:profile is null");
                        }
                    }

                    @Override
                    public void onFailure(Call<GreenFruitProfile> call, Throwable t) {
                        loadingLayout.setVisibility(View.GONE);
                        ToastTools.show(getContext(),"Errot:"+t.getMessage());
                    }
                });
    }

    public void getCourses(GreenFruitTerm.XnxqBean term){
        if(greenFruitProfile==null) return;
        loadingLayout.setVisibility(View.VISIBLE);
        TimetableRequest.getGreenFruitCourse(getContext(), greenFruitProfile.getUserid(), greenFruitProfile.getUsertype(),
                term.getDm(), greenFruitProfile.getToken(), new Callback<GreenFruitCourse>() {
                    @Override
                    public void onResponse(Call<GreenFruitCourse> call, Response<GreenFruitCourse> response) {
                        loadingLayout.setVisibility(View.GONE);
                        GreenFruitCourse fruitCourse=response.body();
                        if(fruitCourse!=null){
                            if(fruitCourse.getWeek1()==null&&fruitCourse.getWeek2()==null&&
                                    fruitCourse.getWeek3()==null&&fruitCourse.getWeek4()==null
                                    &&fruitCourse.getWeek5()==null){
                                ToastTools.show(getContext(),"该学期没有课程,请选择其他学期");
                            }else{
                                saveCourses(fruitCourse);
                                userName.setText("");
                                userPassword.setText("");
                                userSchool.setText("请选择学校");
                                selectSchool=null;
                            }
                        }else {
                            ToastTools.show(getContext(),"Errot:profile is null");
                        }
                    }

                    @Override
                    public void onFailure(Call<GreenFruitCourse> call, Throwable t) {
                        loadingLayout.setVisibility(View.GONE);
                        ToastTools.show(getContext(),"Errot:"+t.getMessage());
                    }
                });
    }

    private void saveCourses(GreenFruitCourse fruitCourse) {
        if(fruitCourse==null) return;
        int curWeek=Integer.parseInt(fruitCourse.getZc());
        List<GreenFruitCourse.WeekBean> weekList1=fruitCourse.getWeek1();
        List<GreenFruitCourse.WeekBean> weekList2=fruitCourse.getWeek2();
        List<GreenFruitCourse.WeekBean> weekList3=fruitCourse.getWeek3();
        List<GreenFruitCourse.WeekBean> weekList4=fruitCourse.getWeek4();
        List<GreenFruitCourse.WeekBean> weekList5=fruitCourse.getWeek5();

        List<TimetableModel> models=new ArrayList<>();
        ScheduleName newName=new ScheduleName();
        newName.setTime(System.currentTimeMillis());
        newName.setName(greenFruitProfile.getXxmc()+"-"+greenFruitProfile.getXm());
        newName.save();

        saveCourses(models,newName,weekList1,1);
        saveCourses(models,newName,weekList2,2);
        saveCourses(models,newName,weekList3,3);
        saveCourses(models,newName,weekList4,4);
        saveCourses(models,newName,weekList5,5);

        ShareTools.putString(getContext(), ShareConstants.STRING_START_TIME, TimetableTools.getStartSchoolTime(curWeek));
        BroadcastUtils.refreshAppWidget(this);
        EventBus.getDefault().post(new UpdateScheduleEvent());
        ToastTools.show(getContext(), "已将当前周调整为:" + curWeek);

        DataSupport.saveAll(models);
        ImportTools.showDialogOnApply(this, newName,true);
    }

    private void saveCourses(List<TimetableModel> models,ScheduleName newName,List<GreenFruitCourse.WeekBean> weekList1,int day) {
        if(weekList1==null) return;
        for(GreenFruitCourse.WeekBean weekBean:weekList1){
            TimetableModel model=new TimetableModel();
            model.setScheduleName(newName);
            model.setName(weekBean.getKcmc());
            model.setRoom(weekBean.getSkdd());
            model.setTeacher(weekBean.getRkjs());
            String[] startAndEnd=weekBean.getJcxx().split("-");
            int start=Integer.parseInt(startAndEnd[0]);
            int end=Integer.parseInt(startAndEnd[1]);
            int step=end-start+1;
            model.setDay(day);
            model.setStart(start);
            model.setStep(step);
            model.setWeekList(TimetableTools.getWeekList(weekBean.getSkzs()));
            models.add(model);
        }
    }

    public void getTerm(final GreenFruitProfile profile){
        if(profile==null) return;
        loadingLayout.setVisibility(View.VISIBLE);
        TimetableRequest.getGreenFruitTerm(getContext(), profile.getUserid(),
                profile.getUsertype(), profile.getToken(),
                new Callback<GreenFruitTerm>() {
                    @Override
                    public void onResponse(Call<GreenFruitTerm> call, Response<GreenFruitTerm> response) {
                        loadingLayout.setVisibility(View.GONE);
                        GreenFruitTerm fruitTerm=response.body();
                        if(fruitTerm!=null){
                            if(fruitTerm.getErrcode()!=null&&!fruitTerm.getErrcode().equals("0")){
                                ToastTools.show(getContext(),"Error:["+fruitTerm.getErrcode()+"] "+fruitTerm.getMessage());
                            }else{
                                termList=fruitTerm;
                                createTermView(fruitTerm);
                            }
                        }else {
                            ToastTools.show(getContext(),"Errot:profile is null");
                        }
                    }

                    @Override
                    public void onFailure(Call<GreenFruitTerm> call, Throwable t) {
                        loadingLayout.setVisibility(View.GONE);
                        ToastTools.show(getContext(),"Errot:"+t.getMessage());
                    }
                });
    }

    private void createTermView(GreenFruitTerm fruitTerm) {
        if(list==null) return;
        termLayout.setVisibility(View.VISIBLE);
        list.clear();
        for(GreenFruitTerm.XnxqBean bean:fruitTerm.getXnxq()){
            if(bean.getDqxq()!=null&&bean.getDqxq().equals("1")){
                list.add(bean.getMc()+" [当前学期]");
            }else list.add(bean.getMc());
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSchoolSelected(SelectSchoolEvent event){
        if(event!=null){
            selectSchool=event.getSchool();
            userSchool.setText(selectSchool.getXxmc());
        }
    }

    @OnClick(R.id.user_select_school_layout)
    public void onSelectSchoolClicked(){
        ActivityTools.toActivityWithout(this,ChooseSchoolActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
