package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard dropdown receiver for compatibility with all ATAK versions
 * This replaces the Pane API approach which is not available in all versions
 */
public class DashboardDropDownReceiver extends DropDownReceiver {
    
    private static final String TAG = "SkyFi.Dashboard";
    private final View dashboardView;
    private final Context pluginContext;
    private final SkyFiPlugin plugin;
    
    public DashboardDropDownReceiver(MapView mapView, Context context) {
        super(mapView);
        this.pluginContext = context;
        this.plugin = SkyFiPlugin.getInstance();
        
        // Inflate the dashboard view
        this.dashboardView = PluginLayoutInflater.inflate(context, R.layout.skyfi_dashboard, null);
        
        // Initialize dashboard UI
        initializeDashboard();
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if ("com.skyfi.atak.SHOW_DASHBOARD".equals(action)) {
            showDropDown();
        }
    }
    
    private void showDropDown() {
        // Show the dashboard as a dropdown
        // Using numeric constants for ATAK 5.3.0 compatibility
        // NINE_TWELFTHS_WIDTH = 0.75, FULL_HEIGHT = 1.0, FULL_WIDTH = 1.0, HALF_HEIGHT = 0.5
        showDropDown(dashboardView,
                0.75, // NINE_TWELFTHS_WIDTH
                1.0,  // FULL_HEIGHT
                1.0,  // FULL_WIDTH
                0.5,  // HALF_HEIGHT
                false);
        
        // Update metrics when showing
        updateDashboardMetrics();
    }
    
    private void initializeDashboard() {
        // Set up click listeners for dashboard cards
        CardView newOrderCard = dashboardView.findViewById(R.id.new_order_card);
        CardView viewOrdersCard = dashboardView.findViewById(R.id.view_orders_card);
        CardView manageAoisCard = dashboardView.findViewById(R.id.manage_aois_card);
        CardView settingsCard = dashboardView.findViewById(R.id.settings_card);
        
        if (newOrderCard != null) {
            newOrderCard.setOnClickListener(v -> {
                closeDropDown();
                plugin.showNewOrderOptions();
            });
        }
        
        if (viewOrdersCard != null) {
            viewOrdersCard.setOnClickListener(v -> {
                closeDropDown();
                Intent intent = new Intent();
                intent.setAction(Orders.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(intent);
            });
        }
        
        if (manageAoisCard != null) {
            manageAoisCard.setOnClickListener(v -> {
                closeDropDown();
                plugin.showAOIManagementDialog();
            });
        }
        
        if (settingsCard != null) {
            settingsCard.setOnClickListener(v -> {
                closeDropDown();
                plugin.showSettingsMenu();
            });
        }
    }
    
    private void updateDashboardMetrics() {
        TextView satelliteCount = dashboardView.findViewById(R.id.satellite_count);
        TextView coveragePercent = dashboardView.findViewById(R.id.coverage_percent);
        TextView activeOrders = dashboardView.findViewById(R.id.active_orders);
        TextView apiStatus = dashboardView.findViewById(R.id.api_status);
        
        // Check API connection status
        if (plugin.apiClient != null) {
            plugin.apiClient.ping().enqueue(new Callback<com.skyfi.atak.plugin.skyfiapi.Pong>() {
                @Override
                public void onResponse(Call<com.skyfi.atak.plugin.skyfiapi.Pong> call, 
                                     Response<com.skyfi.atak.plugin.skyfiapi.Pong> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        getMapView().post(() -> {
                            if (apiStatus != null) {
                                apiStatus.setText("Connected");
                                apiStatus.setTextColor(0xFF4CAF50); // Green
                            }
                        });
                    }
                }
                
                @Override
                public void onFailure(Call<com.skyfi.atak.plugin.skyfiapi.Pong> call, Throwable t) {
                    getMapView().post(() -> {
                        if (apiStatus != null) {
                            apiStatus.setText("Disconnected");
                            apiStatus.setTextColor(0xFFFF5252); // Red
                        }
                    });
                }
            });
        }
        
        // For now, use placeholder values for satellite metrics
        getMapView().post(() -> {
            if (satelliteCount != null) satelliteCount.setText("12");
            if (coveragePercent != null) coveragePercent.setText("87%");
        });
        
        // Get active orders count
        if (plugin.apiClient != null) {
            plugin.apiClient.getOrders(0, 100).enqueue(new Callback<com.skyfi.atak.plugin.skyfiapi.OrderResponse>() {
                @Override
                public void onResponse(Call<com.skyfi.atak.plugin.skyfiapi.OrderResponse> call,
                                     Response<com.skyfi.atak.plugin.skyfiapi.OrderResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                        int activeCount = 0;
                        for (com.skyfi.atak.plugin.skyfiapi.Order order : response.body().getOrders()) {
                            if ("ACTIVE".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                                activeCount++;
                            }
                        }
                        final int count = activeCount;
                        getMapView().post(() -> {
                            if (activeOrders != null) {
                                activeOrders.setText(String.valueOf(count));
                            }
                        });
                    }
                }
                
                @Override
                public void onFailure(Call<com.skyfi.atak.plugin.skyfiapi.OrderResponse> call, Throwable t) {
                    Log.e(TAG, "Failed to get orders", t);
                }
            });
        }
    }
    
    @Override
    public void disposeImpl() {
        // Cleanup if needed
    }
}