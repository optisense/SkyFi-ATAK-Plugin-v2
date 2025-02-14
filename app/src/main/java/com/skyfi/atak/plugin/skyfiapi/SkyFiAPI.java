package com.skyfi.atak.plugin.skyfiapi;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SkyFiAPI {
    // Core
    @GET("/ping")
    Call<Pong> ping();

    @GET("/health_check")
    Call<HealthCheck> healthCheck();

    // Auth
    @GET("/auth/whoami")
    Call<User> whoami();

    // Ordering
    @GET("/platform-api/orders")
    Call<OrderResponse> getOrders();
}
