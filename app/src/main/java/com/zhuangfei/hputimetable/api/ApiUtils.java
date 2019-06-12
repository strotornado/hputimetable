package com.zhuangfei.hputimetable.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhuangfei.hputimetable.api.constants.UrlContacts;

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

    public static Retrofit getRetrofit(Context context) {
        OkHttpClient builder = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder)
                .baseUrl(UrlContacts.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
        return retrofit;
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

    public static Retrofit getRetrofitForGreenFruit(Context context) {
        OkHttpClient builder = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder)
                .baseUrl(UrlContacts.URL_BASE_QINGGUO)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
        return retrofit;
    }
}
