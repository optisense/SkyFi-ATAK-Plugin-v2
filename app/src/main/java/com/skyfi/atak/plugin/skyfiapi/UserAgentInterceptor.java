package com.skyfi.atak.plugin.skyfiapi;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.skyfi.atak.plugin.BuildConfig;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {
    private static final String LOGTAG = "UserAgentInterceptor";
    Context context;

    private void userAgent(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        StringBuilder userAgent = new StringBuilder();
        userAgent.append("SkyFi-ATAK-Plugin ");
        userAgent.append(BuildConfig.VERSION_NAME);
        userAgent.append("(").append(BuildConfig.VERSION_CODE).append(")");
        userAgent.append("(")
                .append(Build.MANUFACTURER).append("; ")
                .append(Build.MODEL).append("; ")
                .append("SDK ").append(Build.VERSION.SDK_INT).append("; ")
                .append("Android ").append(Build.VERSION.RELEASE).append(")");

        Log.d(LOGTAG, "UserAgent: " + userAgent);
        Request.Builder builder = chain.request().newBuilder();
        builder.header("User-Agent", userAgent.toString());
        return chain.proceed(builder.build());
    }
}