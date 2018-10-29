package com.zhuangfei.hputimetable.api;

import android.content.Context;

import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.CheckModel;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
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
}
