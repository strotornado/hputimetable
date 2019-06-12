package com.zhuangfei.hputimetable.api.service;

import com.zhuangfei.hputimetable.api.constants.UrlContacts;
import com.zhuangfei.hputimetable.api.model.GreenFruitCourse;
import com.zhuangfei.hputimetable.api.model.GreenFruitProfile;
import com.zhuangfei.hputimetable.api.model.GreenFruitTerm;
import com.zhuangfei.hputimetable.api.model.ListResult;
import com.zhuangfei.hputimetable.api.model.MajorModel;
import com.zhuangfei.hputimetable.api.model.ObjResult;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.api.model.TimetableResultModel;
import com.zhuangfei.hputimetable.api.model.ValuePair;
import com.zhuangfei.qingguo.GreenFruit;

import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
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

    @POST(UrlContacts.URL_PUT_VALUE)
    @FormUrlEncoded
    Call<ObjResult<ValuePair>> putValue(@Field("value") String value);

    @POST(UrlContacts.URL_GET_VALUE)
    @FormUrlEncoded
    Call<ObjResult<ValuePair>> getValue(@Field("id") String id);

    @POST(UrlContacts.URL_QINGGUO)
    @FormUrlEncoded
    @Headers({"Content-Type:application/x-www-form-urlencoded;"})
    Call<GreenFruitProfile> loginGreenFruit(@Field("param") String param,
                                            @Field("appinfo") String appinfo,
                                            @Field("param2") String param2,
                                            @Field("token") String token);

    @POST(UrlContacts.URL_QINGGUO)
    @FormUrlEncoded
    @Headers({"Content-Type:application/x-www-form-urlencoded;"})
    Call<GreenFruitCourse> getGreenFruitCourse(@Field("param") String param,
                                               @Field("appinfo") String appinfo,
                                               @Field("param2") String param2,
                                               @Field("token") String token);

    @POST(UrlContacts.URL_QINGGUO)
    @FormUrlEncoded
    @Headers({"Content-Type:application/x-www-form-urlencoded;"})
    Call<GreenFruitTerm> getGreenFruitTerm(@Field("param") String param,
                                           @Field("appinfo") String appinfo,
                                           @Field("param2") String param2,
                                           @Field("token") String token);
}
