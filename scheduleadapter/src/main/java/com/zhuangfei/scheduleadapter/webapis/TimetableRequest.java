package com.zhuangfei.scheduleadapter.webapis;

import android.content.Context;

import com.zhuangfei.scheduleadapter.webapis.model.AdapterInfo;
import com.zhuangfei.scheduleadapter.webapis.model.BaseResult;
import com.zhuangfei.scheduleadapter.webapis.model.CheckModel;
import com.zhuangfei.scheduleadapter.webapis.model.HtmlDetail;
import com.zhuangfei.scheduleadapter.webapis.model.HtmlSummary;
import com.zhuangfei.scheduleadapter.webapis.model.ListResult;
import com.zhuangfei.scheduleadapter.webapis.model.MajorModel;
import com.zhuangfei.scheduleadapter.webapis.model.ObjResult;
import com.zhuangfei.scheduleadapter.webapis.model.School;
import com.zhuangfei.scheduleadapter.webapis.model.UserDebugModel;
import com.zhuangfei.scheduleadapter.webapis.model.ValuePair;
import com.zhuangfei.scheduleadapter.webapis.service.SchoolService;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Liu ZhuangFei on 2018/3/2.
 */

public class TimetableRequest {
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
}
