package com.zhuangfei.hputimetable.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.StationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class StationAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<StationModel> list;
    Context context;

    public StationAdapter(Context context, List<StationModel> list) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
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
            convertView = mInflater.inflate(R.layout.item_func_station, null);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.id_layout);
            holder.imageView = (ImageView) convertView.findViewById(R.id.id_img);
            holder.textView = (TextView) convertView.findViewById(R.id.id_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final StationModel stationModel = list.get(position);
        holder.textView.setText(stationModel.getName());
        Glide.with(context).load(stationModel.getImg()).into(holder.imageView);
        return convertView;
    }

    //ViewHolder静态类
    static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public LinearLayout layout;
    }

}
