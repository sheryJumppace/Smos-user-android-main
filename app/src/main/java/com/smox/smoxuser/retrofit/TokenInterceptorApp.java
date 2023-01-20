package com.smox.smoxuser.retrofit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smox.smoxuser.manager.Constants;
import com.smox.smoxuser.manager.SessionManager;
import com.smox.smoxuser.utils.NoConnectivityException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class TokenInterceptorApp implements Interceptor {
    private String token;
    private Context context;
    private SessionManager sessionManager;

    public TokenInterceptorApp(Context context) {
        this.context = context;
        try {

            token = SessionManager.Companion.getInstance(context).getApiKey();
            SessionManager.Companion.getInstance(context).toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        if (!NetworkUtil.isOnline(context)) {
            throw new NoConnectivityException();
        }

        if (token != null && !token.isEmpty()) {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .header("Authentication", token)
                    .build();
            Response response = chain.proceed(request);

            Log.e("intercepter", "token: " + request.header("Authentication"));
            Log.e("response code", "code: " + response.code());

            if (response.code() == 401) {
                Log.e("TAG", "intercept: inside 401 error");
                Intent intent1 = new Intent();
                intent1.setAction(Constants.API.UN_AUTHORISED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                return response;
            }
            return response;
        } else {
            return null;
        }
    }
}
