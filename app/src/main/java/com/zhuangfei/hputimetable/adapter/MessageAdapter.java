package com.zhuangfei.hputimetable.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class MessageAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<MessageModel> list;
    Context context;

    public MessageAdapter(Context context, List<MessageModel> list) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.list=list;
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
            convertView = mInflater.inflate(R.layout.item_message, null);
            holder.contentTextView = (TextView) convertView.findViewById(R.id.item_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final MessageModel model = list.get(position);
        if(model!=null){
            holder.contentTextView.setText(model.getContent());
        }
        return convertView;
    }

    //ViewHolder静态类
    static class ViewHolder {
        public TextView contentTextView;
    }

}
