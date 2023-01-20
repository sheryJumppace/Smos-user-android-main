package com.smox.smoxuser.stripe.module;

import android.content.Context;

import com.smox.smoxuser.manager.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddHeaderInterceptor implements Interceptor {

    SessionManager sessionManager;

    Context mContext;
    public AddHeaderInterceptor(Context mContext){
        this.mContext = mContext;
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Authorization", sessionManager.getApiKey());

        return chain.proceed(builder.build());
    }
}