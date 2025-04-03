package com.skyfi.atak.plugin.skyfiapi;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SkyFiAPI {
    // Core
    @GET("/ping")
    Call<Pong> ping();

    @GET("/health_check")
    Call<HealthCheck> healthCheck();

    // Auth
    @GET("/auth/whoami")
    Call<User> whoami();

    // Orders
    @GET("/platform-api/orders")
    Call<OrderResponse> getOrders(@Query("pageNumber") int pageNumber, @Query("pageSize") int pageSize);

    @POST("/platform-api/order-tasking")
    Call<Order> taskingOrder(@Body TaskingOrder order);

    @POST("/platform-api/order-archive")
    Call<ArchiveResponse> archiveOrder(@Body ArchiveOrder order);

    @POST("/platform-api/archives")
    Call<ArchiveResponse> searchArchives(@Body ArchivesRequest request);

    @GET("/platform-api/archives")
    Call<ArchiveResponse> searchArchivesNextPage(@Query("page") String pageHash);

    @POST("/platform-api/pricing")
    Call<PricingResponse> getTaskingPricing(@Body PricingQuery pricingQuery);

    @GET("/platform-api/auth/whoami")
    Call<MyProfile> getProfile();
}
