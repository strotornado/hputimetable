package com.zhuangfei.hputimetable.adapter;

import android.app.Activity;
import android.text.TextUtils;
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
import com.zhuangfei.hputimetable.activity.WebViewActivity;
import com.zhuangfei.hputimetable.api.TimetableRequest;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.model.SearchResultModel;
import com.zhuangfei.hputimetable.tools.DeviceTools;
import com.zhuangfei.hputimetable.tools.StationManager;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ToastTools;

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

public class SearchSchoolAdapter extends BaseAdapter {

    private static final int TYPE_STATION = 0;
    private static final int TYPE_SCHOOL = 1;
    private static final int TYPE_ITEM_COUNT = 2;
    public static final int TYPE_STATION_MAX_SIZE= 3;

    private LayoutInflater mInflater = null;

    List<SearchResultModel> list;
    List<SearchResultModel> allData;
    Activity context;
    String schoolName="unknow";
    String device;

    public SearchSchoolAdapter(Activity context, List<SearchResultModel> allData,List<SearchResultModel> list) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.list=list;
        this.allData=allData;
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
        SchoolViewHolder schoolViewHolder=null;
        final SearchResultModel model = list.get(position);

        switch (getItemViewType(position)){
            case TYPE_STATION:
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(R.layout.item_search_station, null);
                    holder.stationImageView=convertView.findViewById(R.id.id_search_station_img);
                    holder.stationNameView=convertView.findViewById(R.id.id_station_name);
                    holder.searchTitleView=convertView.findViewById(R.id.id_search_title);
                    holder.lineTextView2=convertView.findViewById(R.id.item_search_line2);
                    holder.stationTagView=convertView.findViewById(R.id.id_station_tag1);
                    holder.moreLayout=convertView.findViewById(R.id.item_search_station_more);
                    holder.moreTextView=convertView.findViewById(R.id.item_search_station_more_text);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.moreLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        list.clear();
                        list.addAll(allData);
                        notifyDataSetChanged();
                    }
                });
                if (model != null) {
                    holder.searchTitleView.setText("服务站");
                    StationModel stationModel= (StationModel) model.getObject();
                    if(stationModel!=null){
                        Glide.with(context).load(stationModel.getImg()).into(holder.stationImageView);
                        holder.stationNameView.setText(stationModel.getName());
                        String tags=stationModel.getTag();
                        if(!TextUtils.isEmpty(tags)){
                            String[] tagArray=tags.split(" ");
                            holder.stationTagView.setText(tagArray[0]);
                        }
                    }

                    if(position==0||model.getType()!=list.get(position-1).getType()){
                        holder.searchTitleView.setVisibility(View.VISIBLE);
                        holder.lineTextView2.setVisibility(View.VISIBLE);
                    }else {
                        holder.searchTitleView.setVisibility(View.GONE);
                        holder.lineTextView2.setVisibility(View.GONE);
                    }

                    if(position==0){
                        holder.lineTextView2.setVisibility(View.GONE);
                    }

                    if((position==(list.size()-1)||
                            model.getType()!=list.get(position+1).getType())&&
                            model.getType()==SearchResultModel.TYPE_STATION_MORE){
                        holder.moreLayout.setVisibility(View.VISIBLE);
                        if(allData.size()-list.size()<=0){
                            holder.moreTextView.setText("查看更多");
                        }else{
                            holder.moreTextView.setText("查看更多("+(allData.size()-list.size())+")");
                        }
                    }else {
                        holder.moreLayout.setVisibility(View.GONE);
                    }
                }
                break;
            case TYPE_SCHOOL:
                if (convertView == null) {
                    schoolViewHolder = new SchoolViewHolder();
                    convertView = mInflater.inflate(R.layout.item_search_school, null);
                    schoolViewHolder.searchTitleView=convertView.findViewById(R.id.id_search_title);
                    schoolViewHolder.lineTextView2=convertView.findViewById(R.id.item_search_line2);
                    schoolViewHolder.schoolLayout=convertView.findViewById(R.id.id_search_school_layout);
                    schoolViewHolder.schoolTextView=convertView.findViewById(R.id.item_school_val);
                    schoolViewHolder.schoolTypeTextView=convertView.findViewById(R.id.id_search_school_type);
                    convertView.setTag(schoolViewHolder);
                } else {
                    schoolViewHolder = (SchoolViewHolder) convertView.getTag();
                }
                if (model != null) {
                    schoolViewHolder.searchTitleView.setText("教务导入");
                    schoolViewHolder.schoolLayout.setVisibility(View.VISIBLE);
                    School school= (School) model.getObject();
                    if(school!=null){
                        schoolViewHolder.schoolTextView.setText(school.getSchoolName());
                        String type=school.getType();
                        if(!TextUtils.isEmpty(type)){
                            if(type.endsWith("教务")){
                                type=type.replaceAll("教务","");
                            }
                            if(type.endsWith("教务系统")){
                                type=type.replaceAll("教务系统","");
                            }
                        }else {
                            type="未知";
                        }
                        schoolViewHolder.schoolTypeTextView.setText(type);
                    }

                    if(position==0||model.getType()!=list.get(position-1).getType()){
                        schoolViewHolder.searchTitleView.setVisibility(View.VISIBLE);
                        schoolViewHolder.lineTextView2.setVisibility(View.VISIBLE);
                    }else {
                        schoolViewHolder.searchTitleView.setVisibility(View.GONE);
                        schoolViewHolder.lineTextView2.setVisibility(View.GONE);
                    }

                    if(position==0){
                        schoolViewHolder.lineTextView2.setVisibility(View.GONE);
                    }
                }
                break;
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getType()==SearchResultModel.TYPE_STATION||
                list.get(position).getType()==SearchResultModel.TYPE_STATION_MORE){
            return TYPE_STATION;
        }else{
            return TYPE_SCHOOL;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_ITEM_COUNT;
    }

    //ViewHolder静态类
    static class ViewHolder {
        public TextView searchTitleView;
        public TextView stationTagView;
        public View lineTextView2;
        public ImageView stationImageView;
        public TextView stationNameView;
        public TextView moreTextView;
        public LinearLayout moreLayout;
    }

    //ViewHolder静态类
    static class SchoolViewHolder {
        public TextView searchTitleView;
        public View lineTextView2;
        public LinearLayout schoolLayout;
        public TextView schoolTextView;
        public TextView schoolTypeTextView;
    }

}