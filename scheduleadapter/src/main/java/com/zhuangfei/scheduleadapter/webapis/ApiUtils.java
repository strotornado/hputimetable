package com.zhuangfei.scheduleadapter.webapis;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhuangfei.scheduleadapter.webapis.constants.UrlContacts;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Liu ZhuangFei on 2018/3/2.
 */

public class ApiUtils {
    public static Gson getGson() {
        Gson gson = new GsonBuilder()
                //配置你的Gson
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
        return gson;
    }

    public static Retrofit getRetrofitForSchool(Context context) {
        OkHttpClient builder = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder)
                .baseUrl(UrlContacts.URL_BASE_SCHOOLS)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
        return retrofit;
    }
}
