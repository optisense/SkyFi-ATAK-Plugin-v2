package com.skyfi.atak.plugin.skyfiapi;

import com.skyfi.atak.plugin.Pong;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SkyFiAPI {
    @GET("https://app.skyfi.com/platform-api/ping")
    Call<Pong> ping();
}
