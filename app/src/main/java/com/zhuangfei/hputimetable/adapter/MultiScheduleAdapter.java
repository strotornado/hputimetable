package com.zhuangfei.hputimetable.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class MultiScheduleAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<ScheduleName> list;
    Context context;
    SimpleDateFormat sdf;

    public MultiScheduleAdapter(Context context, List<ScheduleName> list) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        sdf=new SimpleDateFormat("MM/dd HH:mm");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_multi, null);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.item_multi_name);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.item_multi_time);
            holder.lightView=convertView.findViewById(R.id.item_timeline_lightbgview);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ScheduleName scheduleName = list.get(position);

        if(position==0){
            holder.lightView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_multi_timeline_light));
            holder.timeTextView.setText("当前课表");
            holder.timeTextView.setTextColor(context.getResources().getColor(R.color.app_white));
        }else{
            holder.lightView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_multi_timeline_gray));
            holder.timeTextView.setText(sdf.format(new Date(scheduleName.getTime())));
        }

        holder.nameTextView.setText(scheduleName.getName());
        return convertView;
    }

    //ViewHolder静态类
    static class ViewHolder {
        public TextView nameTextView;
        public TextView timeTextView;
        public View lightView;
    }

}
