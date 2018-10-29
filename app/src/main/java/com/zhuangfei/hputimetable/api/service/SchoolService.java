package com.zhuangfei.hputimetable.api.service;

import com.zhuangfei.hputimetable.api.constants.UrlContacts;
import com.zhuangfei.hputimetable.api.model.BaseResult;
import com.zhuangfei.hputimetable.api.model.CheckModel;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.School;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Liu ZhuangFei on 2018/2/23.
 */

public interface SchoolService {

    @POST(UrlContacts.URL_GET_ADAPTER_SCHOOLS)
    @FormUrlEncoded
    Call<ListResult<School>> getAdapterSchools(@Field("key") String key);

    @POST(UrlContacts.URL_PUT_HTML)
    @FormUrlEncoded
    Call<BaseResult> putHtml(@Field("school") String school,
                             @Field("url") String url,
                             @Field("html") String html);

    @POST(UrlContacts.URL_CHECK_SCHOOL)
    @FormUrlEncoded
    Call<ObjResult<CheckModel>> checkSchool(@Field("school") String school);
 }
