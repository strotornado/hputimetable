package com.zhuangfei.hputimetable;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class SelectWeekAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<Boolean> statusList;
    Context context;

    public SelectWeekAdapter(Context context,List<Boolean> statusList) {
        this.mInflater = LayoutInflater.from(context);
        this.statusList=statusList;
        this.context=context;
    }

    @Override
    public int getCount() {
        return statusList.size();
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
            convertView = mInflater.inflate(R.layout.item_select_week, null);
            holder.textView = (TextView) convertView.findViewById(R.id.item_text);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(statusList.get(position)){
            holder.textView.setTextColor(Color.WHITE);
            holder.textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.week_selected_bg));
        }else{
            holder.textView.setBackgroundDrawable(null);
            holder.textView.setTextColor(Color.BLACK);
        }

        holder.textView.setText(""+(position+1));
        final ViewHolder finalHolder = holder;
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(statusList.get(position)){
                    statusList.set(position,false);
                    finalHolder.textView.setBackgroundDrawable(null);
                    finalHolder.textView.setTextColor(Color.BLACK);
                }else{
                    statusList.set(position,true);
                    finalHolder.textView.setTextColor(Color.WHITE);
                    finalHolder.textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.week_selected_bg));
                }
            }
        });

        return convertView;
    }

    //ViewHolder静态类
    static class ViewHolder
    {
        public TextView textView;
    }

}
