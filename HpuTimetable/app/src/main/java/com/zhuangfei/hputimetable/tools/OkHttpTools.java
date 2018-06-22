package com.zhuangfei.hputimetable.tools;

import android.annotation.SuppressLint;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 网络请求的工具类，主要对OkHttp这个框架进行二次封装，
 * 目前只有两个Get的方法
 * @author Administrator
 *
 */
public class OkHttpTools {

	private static OkHttpClient okHttpClient = null;
	
	private static OkHttpClient getInstance() {
		if(okHttpClient==null){
			okHttpClient = new OkHttpClient.Builder()
					.connectTimeout(120,TimeUnit.SECONDS)
					.writeTimeout(120,TimeUnit.SECONDS)
					.readTimeout(120,TimeUnit.SECONDS)
					.sslSocketFactory(createSSLSocketFactory())
					.build();
		}
		return  okHttpClient;
	}

	/**
	 * 默认信任所有的证书
	 * TODO 最好加上证书认证，主流App都有自己的证书
	 *
	 * @return
	 */
	@SuppressLint("TrulyRandom")
	private static SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory sSLSocketFactory = null;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[]{new TrustAllManager()},
					new SecureRandom());
			sSLSocketFactory = sc.getSocketFactory();
		} catch (Exception e) {
		}
		return sSLSocketFactory;
	}

	private static class TrustAllManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}

	private static class TrustAllHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}


	/**
	 * GET请求，callback是回调接口
	 * @param url
	 * @param callback
	 */
	public static void get(String url, Callback callback) {
		okHttpClient = getInstance();
		Request request = new Request.Builder().url(url).build();
		okHttpClient.newCall(request).enqueue(callback);

	}

	/**
	 * 携带Cookie的GET请求，callback是回调接口
	 * @param url
	 * @param callback
	 */
	public static void getByCookie(String url, String cookie, Callback callback) {
		okHttpClient = getInstance();
		Request request = new Request.Builder().url(url).addHeader("Cookie", cookie).build();
		okHttpClient.newCall(request).enqueue(callback);
	}

	public static void post(String url,Callback callback,String[] keys,String[] values){
		okHttpClient = getInstance();
		FormBody.Builder builder = new FormBody.Builder();
		for(int i=0;i<keys.length;i++){
			builder.add(keys[i],values[i]);
		}

		RequestBody formBody = builder.build();
		Request request = new Request.Builder()
				.url(url)
				.post(formBody)
				.build();
		okHttpClient.newCall(request).enqueue(callback);
	}
	
	public static void upLoadFile(String url,String cookie, HashMap<String, Object> paramsMap, Callback callBack) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse("image/png"), file));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(url).post(body).addHeader("Cookie", cookie).build();
            //单独设置参数 比如读取超时时间
            final Call call = getInstance().newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
            call.enqueue(callBack);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

	public static void postByCookie(String url,String cookie,Callback callback,String[] keys,String[] values){
		okHttpClient = getInstance();
		FormBody.Builder builder = new FormBody.Builder();
		for(int i=0;i<keys.length;i++){
			builder.add(keys[i],values[i]);
		}

		RequestBody formBody = builder.build();
		Request request = new Request.Builder()
				.url(url)
				.post(formBody)
				.addHeader("Cookie", cookie)
				.build();
		okHttpClient.newCall(request).enqueue(callback);
	}
}
