package com.zhuangfei.hputimetable.api;

import android.content.Context;

import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
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
}
