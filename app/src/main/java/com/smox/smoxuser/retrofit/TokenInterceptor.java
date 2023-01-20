package com.smox.smoxuser.retrofit;

import android.content.Context;
import android.util.Log;

import com.smox.smoxuser.manager.Constants;
import com.smox.smoxuser.utils.NoConnectivityException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class TokenInterceptor implements Interceptor  {
    private String token;
    private Context context;

    public TokenInterceptor(Context context) {
        this.context = context;
        try {

            token = Constants.KStripe.secretKey;
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

            //Log.e("okhttp", "token: " + token);

            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            Response response = chain.proceed(request);

            Log.e("intercepter", "token: " + request.header("Authorization"));


            if (response.code() == 401) {
                Log.d("TAG", "intercept: inside 401 error");
                return response;
            }
            return response;
        } else {
            /*Preferences.saveBoolean(context, ConstantValue.KEY_IS_LOGIN, false);
            context.startActivity(new Intent(context, SignInActivity.class));*/
            return null;
        }
    }
}
