package com.zhuangfei.scheduleadapter.webapis.service;

import com.zhuangfei.scheduleadapter.webapis.constants.UrlContacts;
import com.zhuangfei.scheduleadapter.webapis.model.AdapterInfo;
import com.zhuangfei.scheduleadapter.webapis.model.BaseResult;
import com.zhuangfei.scheduleadapter.webapis.model.CheckModel;
import com.zhuangfei.scheduleadapter.webapis.model.HtmlDetail;
import com.zhuangfei.scheduleadapter.webapis.model.HtmlSummary;
import com.zhuangfei.scheduleadapter.webapis.model.ListResult;
import com.zhuangfei.scheduleadapter.webapis.model.ObjResult;
import com.zhuangfei.scheduleadapter.webapis.model.School;
import com.zhuangfei.scheduleadapter.webapis.model.UserDebugModel;

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

    @POST(UrlContacts.URL_GET_USER_INFO)
    @FormUrlEncoded
    Call<ObjResult<UserDebugModel>> getUserInfo(@Field("name") String name, @Field("id") String id);

    @POST(UrlContacts.URL_FIND_HTML_SUMMARY)
    @FormUrlEncoded
    Call<ListResult<HtmlSummary>> findHtmlummary(@Field("school") String schoolName);

    @POST(UrlContacts.URL_FIND_HTML_DETAIL)
    @FormUrlEncoded
    Call<ObjResult<HtmlDetail>> findHtmlDetail(@Field("filename") String schoolName);

    @POST(UrlContacts.URL_GET_ADAPTER_INFO)
    @FormUrlEncoded
    Call<ObjResult<AdapterInfo>> getAdapterInfo(@Field("key") String uid,
                                                @Field("aid") String aid);
 }
