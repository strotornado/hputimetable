package com.zhuangfei.hputimetable.timetable_custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.listener.OnWeekItemClickedAdapter;
import com.zhuangfei.timetable.listener.OnWeekLeftClickedAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.model.WeekViewEnable;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.timetable.view.PerWeekView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/24.
 */
public class CustomWeekView extends LinearLayout implements WeekViewEnable<CustomWeekView> {

    LayoutInflater mInflate;

    //周次的容器
    LinearLayout container;
    HorizontalScrollView scrollView;

    //跟布局
    LinearLayout root;

    //左侧按钮
    LinearLayout leftlayout;

    //数据
    private List<Schedule> dataSource;

    //布局保存
    private List<LinearLayout> layouts;
    private List<TextView> textViews;

    //当前周
    private int curWeek=1;
    private int preIndex=1;

    //多少项
    private int itemCount = 20;

    private IWeekView.OnWeekItemClickedListener onWeekItemClickedListener;
    private IWeekView.OnWeekLeftClickedListener onWeekLeftClickedListener;

    public CustomWeekView(Context context) {
        this(context, null);
    }

    /**
     * 获取Item点击监听
     * @return
     */
    public IWeekView.OnWeekItemClickedListener onWeekItemClickedListener() {
        if(onWeekItemClickedListener==null) onWeekItemClickedListener=new OnWeekItemClickedAdapter();
        return onWeekItemClickedListener;
    }

    /**
     * 设置Item点击监听
     * @param onWeekItemClickedListener
     * @return
     */
    public CustomWeekView callback(IWeekView.OnWeekItemClickedListener onWeekItemClickedListener) {
        this.onWeekItemClickedListener = onWeekItemClickedListener;
        return this;
    }

    /**
     * 获取左侧按钮点击监听
     * @return
     */
    public IWeekView.OnWeekLeftClickedListener onWeekLeftClickedListener() {
        if(onWeekLeftClickedListener==null) onWeekLeftClickedListener=new OnWeekLeftClickedAdapter();
        return onWeekLeftClickedListener;
    }

    /**
     * 设置左侧按钮点击监听
     * @param onWeekLeftClickedListener
     * @return
     */
    public CustomWeekView callback(IWeekView.OnWeekLeftClickedListener onWeekLeftClickedListener) {
        this.onWeekLeftClickedListener = onWeekLeftClickedListener;
        return this;
    }

    /**
     * 设置当前周
     * @param curWeek
     * @return
     */
    @Override
    public CustomWeekView curWeek(int curWeek) {
        if(curWeek<1) curWeek=1;
        this.curWeek = curWeek;
        return this;
    }

    /**
     * 设置项数
     * @param count
     * @return
     */
    @Override
    public CustomWeekView itemCount(int count) {
        if (count <= 0) return this;
        this.itemCount = count;
        return this;
    }

    @Override
    public int itemCount() {
        return itemCount;
    }

    /**
     * 设置数据源
     * @param list
     * @return
     */
    @Override
    public CustomWeekView source(List<? extends ScheduleEnable> list) {
        data(ScheduleSupport.transform(list));
        return this;
    }

    /**
     * 设置数据源
     * @param scheduleList
     * @return
     */
    @Override
    public CustomWeekView data(List<Schedule> scheduleList) {
        if (scheduleList == null) return null;
        this.dataSource = scheduleList;
        return this;
    }

    /**
     * 获取数据源
     * @return
     */
    @Override
    public List<Schedule> dataSource() {
        if (dataSource == null) dataSource = new ArrayList<>();
        return dataSource;
    }

    public CustomWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflate = LayoutInflater.from(context);
        initView();
    }

    private void initView() {
        mInflate.inflate(R.layout.view_custom_weekview, this);
        container = findViewById(R.id.id_weekview_container);
        root=findViewById(R.id.id_root);
        leftlayout=findViewById(R.id.id_weekview_leftlayout);
        scrollView=findViewById(R.id.id_weekview_scrollview);
    }

    /**
     * 初次构建时调用，显示周次选择布局
     */
    @Override
    public CustomWeekView showView() {
        container.removeAllViews();
        layouts=new ArrayList<>();
        textViews=new ArrayList<>();

        leftlayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekLeftClickedListener().onWeekLeftClicked();
            }
        });

        for (int i = 1; i <= itemCount; i++) {
            final int tmp=i;
            View view = mInflate.inflate(R.layout.item_weekview, null);
            final LinearLayout perLayout=view.findViewById(R.id.id_perweekview_layout);
            TextView weekText = view.findViewById(R.id.id_weektext);
            TextView bottomText = view.findViewById(R.id.id_weektext_bottom);

            weekText.setText("第" + i + "周");
            if(i==curWeek) bottomText.setText("(本周)");
            PerWeekView perWeekView = view.findViewById(R.id.id_perweekview);
            perWeekView.setData(dataSource(), i);
            perLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetBackground();
                    preIndex=tmp;
                    perLayout.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.weekview_white));
                    onWeekItemClickedListener().onWeekClicked(tmp);
                }
            });

            layouts.add(perLayout);
            textViews.add(bottomText);
            container.addView(view);
        }
        if(curWeek>0&&curWeek<=layouts.size()){
            layouts.get(curWeek-1).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.weekview_thisweek));
        }
        return this;
    }

    /**
     * 当前周被改变后可以调用该方式修正一下底部的文本
     * @return
     */
    @Override
    public CustomWeekView updateView(){
        if(layouts==null||layouts.size()==0) return this;
        if(textViews==null||textViews.size()==0) return this;

        for(int i=0;i<layouts.size();i++){
            if(curWeek-1==i) {
                textViews.get(i).setText("(本周)");
            }
            else{
                textViews.get(i).setText("");
            }
            layouts.get(i).setBackgroundColor(getContext().getResources().getColor(R.color.app_course_chooseweek_bg));
        }

        if(curWeek>0&&curWeek<=layouts.size()){
            layouts.get(curWeek-1).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.weekview_thisweek));
        }
        return this;
    }

    /**
     * 重置背景色
     */
    public void resetBackground(){
        layouts.get(preIndex-1).setBackgroundColor(getContext().getResources().getColor(R.color.app_course_chooseweek_bg));
        layouts.get(curWeek-1).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.weekview_thisweek));
    }

    /**
     * 隐藏左侧设置当前周的控件
     */
    public CustomWeekView hideLeftLayout(){
        leftlayout.setVisibility(View.GONE);
        return this;
    }

    /**
     * 设置控件的可见性
     * @param isShow true:显示，false:隐藏
     */
    @Override
    public CustomWeekView isShow(boolean isShow){
        if(isShow){
            root.setVisibility(VISIBLE);
        }else{
            root.setVisibility(GONE);
        }
        return this;
    }

    /**
     * 判断该控件是否显示
     * @return
     */
    @Override
    public boolean isShowing(){
        if(root.getVisibility()==GONE) return false;
        return true;
    }

    /**
     * 将周次的滚动条切换到索引处
     *
     * @param index 0：第一个位置
     */
    public void scrollToIndex(final int index){
        int c=container.getChildCount();
        if(index<0||index>=c) return;
        final LinearLayout layout=layouts.get(index);
        if(layout!=null){
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("ScrollView", "run: ");
                    scrollView.scrollTo(index* ScreenUtils.dip2px(getContext(),65),0);
                }
            },200);
        }
    }
}
