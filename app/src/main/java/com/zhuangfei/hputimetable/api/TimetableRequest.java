package com.zhuangfei.hputimetable.api;

import android.content.Context;

import com.zhuangfei.hputimetable.api.model.AdapterInfo;
import com.zhuangfei.hputimetable.api.model.AdapterResultV2;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.CheckBindResultModel;
import com.zhuangfei.hputimetable.api.model.CheckModel;
import com.zhuangfei.hputimetable.api.model.HtmlDetail;
import com.zhuangfei.hputimetable.api.model.HtmlSummary;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.MessageModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.SchoolPersonModel;
import com.zhuangfei.hputimetable.api.model.StationModel;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.api.model.UserDebugModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.hputimetable.api.service.SchoolService;
import com.zhuangfei.hputimetable.api.service.TimetableService;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Liu ZhuangFei on 2018/3/2.
 */

public class TimetableRequest {

    public static void getByMajor(Context context, String major,Callback<ObjResult<TimetableResultModel>> callback) {
        TimetableService timetableService = ApiUtils.getRetrofit(context)
                .create(TimetableService.class);
        Call<ObjResult<TimetableResultModel>> call = timetableService.getByMajor(major);
        call.enqueue(callback);
    }

    public static void findMajor(Context context, String major,Callback<ListResult<MajorModel>> callback) {
        TimetableService timetableService = ApiUtils.getRetrofit(context)
                .create(TimetableService.class);
        Call<ListResult<MajorModel>> call = timetableService.findMajor(major);
        call.enqueue(callback);
    }

    public static void getByName(Context context, String name,Callback<ListResult<TimetableModel>> callback) {
        TimetableService timetableService = ApiUtils.getRetrofit(context)
                .create(TimetableService.class);
        Call<ListResult<TimetableModel>> call = timetableService.getByName(name);
        call.enqueue(callback);
    }

    public static void putValue(Context context, String val,Callback<ObjResult<ValuePair>> callback) {
        TimetableService timetableService = ApiUtils.getRetrofit(context)
                .create(TimetableService.class);
        Call<ObjResult<ValuePair>> call = timetableService.putValue(val);
        call.enqueue(callback);
    }

    public static void getValue(Context context, String id,Callback<ObjResult<ValuePair>> callback) {
        TimetableService timetableService = ApiUtils.getRetrofit(context)
                .create(TimetableService.class);
        Call<ObjResult<ValuePair>> call=timetableService.getValue(id);
        call.enqueue(callback);
    }

    /**
     * 获取适配学校列表
     * @param context
     * @param callback
     */
    public static void getAdapterSchools(Context context,String key,Callback<ListResult<School>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ListResult<School>> call=schoolService.getAdapterSchools(key);
        call.enqueue(callback);
    }

    public static void putHtml(Context context,String school,String url,String html,Callback<BaseResult> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<BaseResult> call=schoolService.putHtml(school,url,html);
        call.enqueue(callback);
    }

    public static void checkSchool(Context context,String school,Callback<ObjResult<CheckModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<CheckModel>> call=schoolService.checkSchool(school);
        call.enqueue(callback);
    }

    public static void getUserInfo(Context context,String name,String uid,Callback<ObjResult<UserDebugModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<UserDebugModel>> call=schoolService.getUserInfo(name,uid);
        call.enqueue(callback);
    }

    public static void findHtmlSummary(Context context,String schoolName,Callback<ListResult<HtmlSummary>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ListResult<HtmlSummary>> call=schoolService.findHtmlummary(schoolName);
        call.enqueue(callback);
    }

    public static void findHtmlDetail(Context context,String filename,Callback<ObjResult<HtmlDetail>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<HtmlDetail>> call=schoolService.findHtmlDetail(filename);
        call.enqueue(callback);
    }

    public static void getAdapterInfo(Context context,String uid,String aid,Callback<ObjResult<AdapterInfo>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<AdapterInfo>> call=schoolService.getAdapterInfo(uid,aid);
        call.enqueue(callback);
    }

    public static void getStations(Context context,String key,Callback<ListResult<StationModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ListResult<StationModel>> call=schoolService.getStations(key);
        call.enqueue(callback);
    }

    public static void getMessages(Context context,String device,String school,String mode,Callback<ListResult<MessageModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ListResult<MessageModel>> call=schoolService.getMessages(device,school,mode==null?"":mode);
        call.enqueue(callback);
    }

    public static void setMessageRead(Context context,int messageId,Callback<BaseResult> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<BaseResult> call=schoolService.setMessageRead(messageId);
        call.enqueue(callback);
    }

    public static void bindSchool(Context context,String device,String school,Callback<BaseResult> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<BaseResult> call=schoolService.bindSchool(device,school);
        call.enqueue(callback);
    }

    public static void getSchoolPersonCount(Context context,String school,Callback<ObjResult<SchoolPersonModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<SchoolPersonModel>> call=schoolService.getSchoolPersonCount(school);
        call.enqueue(callback);
    }

    public static void checkIsBindSchool(Context context,String device,Callback<ObjResult<CheckBindResultModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<CheckBindResultModel>> call=schoolService.checkIsBindSchool(device);
        call.enqueue(callback);
    }

    public static void getStationById(Context context,int id,Callback<ListResult<StationModel>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ListResult<StationModel>> call=schoolService.getStationById(id);
        call.enqueue(callback);
    }

    public static void getAdapterSchoolsV2(Context context,String key,Callback<ObjResult<AdapterResultV2>> callback) {
        SchoolService schoolService=ApiUtils.getRetrofitForSchool(context).create(SchoolService.class);
        Call<ObjResult<AdapterResultV2>> call=schoolService.getAdapterSchoolsV2(key);
        call.enqueue(callback);
    }

}
