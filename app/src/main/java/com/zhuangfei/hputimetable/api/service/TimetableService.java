package com.zhuangfei.hputimetable.api.service;

import com.zhuangfei.hputimetable.api.constants.UrlContacts;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Liu ZhuangFei on 2018/2/23.
 */

public interface TimetableService {

    @POST(UrlContacts.URL_GET_BY_MAJOR)
    @FormUrlEncoded
    Call<ObjResult<TimetableResultModel>> getByMajor(@Field("major") String major);

    @POST(UrlContacts.URL_FIND_MAJOR)
    @FormUrlEncoded
    Call<ListResult<MajorModel>> findMajor(@Field("major") String major);

    @POST(UrlContacts.URL_GET_BY_NAME)
    @FormUrlEncoded
    Call<ListResult<TimetableModel>> getByName(@Field("name") String name);
 }
