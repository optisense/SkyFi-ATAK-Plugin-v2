package com.skyfi.atak.plugin;

import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;
import com.skyfi.atak.plugin.skyfiapi.UserAgentInterceptor;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
// import okhttp3.logging.HttpLoggingInterceptor; // Not available at runtime
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private final static String LOGTAG = "SkyFiAPIClient";
    private OkHttpClient.Builder okHttpClient;
    private Retrofit retrofit;
    private SkyFiAPI skyFiAPI;

    public APIClient() {
        Preferences prefs = new Preferences();

        okHttpClient = new OkHttpClient.Builder();
        okHttpClient.addInterceptor(new UserAgentInterceptor());
        okHttpClient.addInterceptor(new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                builder.header("X-Skyfi-Api-Key", prefs.getApiKey());
                return chain.proceed(builder.build());
            }
        });
        // HttpLoggingInterceptor removed - not available at runtime in ATAK
        // Logging can be done through Android Log class if needed

        retrofit = new Retrofit.Builder()
                .client(okHttpClient.build())
                .baseUrl("https://app.skyfi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        skyFiAPI = retrofit.create(SkyFiAPI.class);
    }

    public SkyFiAPI getApiClient() {
        return skyFiAPI;
    }
}
