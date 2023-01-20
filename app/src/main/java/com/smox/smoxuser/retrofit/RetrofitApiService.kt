package com.smox.smoxuser.retrofit

import android.content.Context
import com.google.gson.GsonBuilder
import com.smox.smoxuser.manager.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitApiService {

    var BASE_URL = "https://api.stripe.com/v1/"
    var APP_BASE_URL = Constants.KUrl.server

    private fun getHttpLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private fun getHttpClientForAuth(context: Context): OkHttpClient {
        return OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(getHttpLogging()).addInterceptor(TokenInterceptor(context)).build()
    }

    private fun getHttpClientForAuthForApp(context: Context): OkHttpClient {
        return OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(getHttpLogging()).addInterceptor(TokenInterceptorApp(context)).build()
    }

    private fun getRetrofitClientForAuth(context: Context): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder().serializeNulls()
                .create())).client(getHttpClientForAuth(context))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    }

    fun getRetrofitServiceForAuth(context: Context): ApiInterface {
        return getRetrofitClientForAuth(context).create(ApiInterface::class.java)
    }

    private fun getRetrofitClientForAuthForApp(context: Context): Retrofit {
        return Retrofit.Builder().baseUrl(APP_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder().serializeNulls()
                    .create())).client(getHttpClientForAuthForApp(context))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    }

    fun getRetrofitServiceForAuthForApp(context: Context): ApiInterface {
        return getRetrofitClientForAuthForApp(context).create(ApiInterface::class.java)
    }

}