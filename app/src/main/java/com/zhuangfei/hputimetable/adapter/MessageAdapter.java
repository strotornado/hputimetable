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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zhuangfei.hputimetable.R;
import com.zhuangfei.hputimetable.activity.MessageActivity;
import com.zhuangfei.hputimetable.activity.StationWebViewActivity;
import com.zhuangfei.hputimetable.activity.WebViewActivity;
import com.zhuangfei.hputimetable.activity.adapter.SearchSchoolActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class MessageAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<MessageModel> list;
    Activity context;
    String schoolName="unknow";
    String device;

    public MessageAdapter(Activity context, List<MessageModel> list) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
        device=DeviceTools.getDeviceId(context);
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
            holder.targetTextView = (TextView) convertView.findViewById(R.id.item_message_target);
            holder.stationLayout = (LinearLayout) convertView.findViewById(R.id.item_station_layout);
            holder.urlLayout = (LinearLayout) convertView.findViewById(R.id.item_url_layout);
            holder.urlTitleTextView = (TextView) convertView.findViewById(R.id.item_message_url_title);
            holder.stationNameTextView = (TextView) convertView.findViewById(R.id.item_message_station_name);
            holder.stationImageView = (ImageView) convertView.findViewById(R.id.item_message_station_img);
            holder.readTextView=convertView.findViewById(R.id.item_message_isread);
            holder.timeTextView=convertView.findViewById(R.id.item_message_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final MessageModel model = list.get(position);
        if (model != null) {
            SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm");
            if(model.getTime()!=null){
                holder.timeTextView.setText(sdf.format(new Date(Long.parseLong(model.getTime()+"000"))));
            }else {
                holder.timeTextView.setText("未知时间");
            }

            if(model.getIsread()==0){
                holder.readTextView.setVisibility(View.VISIBLE);
            }else {
                holder.readTextView.setVisibility(View.GONE);
            }

            holder.targetTextView.setVisibility(View.VISIBLE);
            if(model.getTarget()!=null&&device!=null){
                if(model.getTarget().equals(device)){
                    holder.targetTextView.setText("To 当前设备");
                }else  if(model.getTarget().equals(schoolName)){
                    holder.targetTextView.setText("To 当前学校");
                }else if(model.getTarget().equals("all")){
                    holder.targetTextView.setText("To 所有用户");
                }else {
                    holder.targetTextView.setVisibility(View.GONE);
                }
            }else {
                holder.targetTextView.setVisibility(View.GONE);
            }

            final TextView finalTextView=holder.readTextView;
            holder.readTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setMessageRead(model.getId(),finalTextView);
                }
            });

            final String content = model.getContent();
            if (content != null) {
                String realContent = content.replaceAll("<station>.*?</station>", "");
                realContent = realContent.replaceAll("<url>.*?</url>", "");
                holder.contentTextView.setText(realContent);

                Pattern pattern = Pattern.compile("<station>(.*?)</station>");
                Matcher matcher = pattern.matcher(content);
                boolean isFind = matcher.find();
                if (isFind) {
                    final Map<String, String> map = new HashMap();
                    String stationInfo = matcher.group(1);
                    String[] array = stationInfo.split("&next;");
                    if (array != null) {
                        for (int i = 0; i < array.length; i++) {
                            String[] params = array[i].split("=");
                            map.put(params[0], params[1]);
                        }
                    }
                    holder.stationLayout.setVisibility(View.VISIBLE);
                    holder.stationNameTextView.setText(map.get("name"));
                    holder.stationLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StationManager.openStationWithout(context,map.get("url"),map.get("name"));
                        }
                    });
                    Glide.with(context).load(map.get("img")).into(holder.stationImageView);
                } else {
                    holder.stationLayout.setVisibility(View.GONE);
                }

                Pattern pattern2 = Pattern.compile("<url>(.*?)</url>");
                Matcher matcher2 = pattern2.matcher(content);
                boolean isFind2 = matcher2.find();
                if (isFind2) {
                    final Map<String, String> map = new HashMap();
                    String urlInfo = matcher2.group(1);
                    String[] array = urlInfo.split("&next;");
                    if (array != null) {
                        for (int i = 0; i < array.length; i++) {
                            String[] params = array[i].split("=");
                            map.put(params[0], params[1]);
                        }
                    }
                    holder.urlLayout.setVisibility(View.VISIBLE);
                    holder.urlTitleTextView.setText(map.get("title"));
                    holder.urlLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityTools.toActivityWithout(context, WebViewActivity.class,
                                    new BundleModel().setFromClass(context.getClass())
                                            .put("title", map.get("title"))
                                            .put("url",map.get("href")));
                        }
                    });
                } else {
                    holder.urlLayout.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    public void setMessageRead(int id, final TextView readTextView){
        if(id==0) return;

        TimetableRequest.setMessageRead(context, id, new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                if(response==null) return;
                BaseResult baseResult=response.body();
                if(baseResult==null) return;
                if(baseResult.getCode()==200){
                    if(readTextView!=null){
                        readTextView.setVisibility(View.GONE);
                    }
                    Toast.makeText(context,"已标为已读!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,baseResult.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toast.makeText(context,"Error:"+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //ViewHolder静态类
    static class ViewHolder {
        public TextView timeTextView;
        public TextView readTextView;
        public TextView targetTextView;
        public TextView contentTextView;
        public LinearLayout stationLayout;
        public ImageView stationImageView;
        public TextView stationNameTextView;
        public LinearLayout urlLayout;
        public TextView urlTitleTextView;
    }

}
