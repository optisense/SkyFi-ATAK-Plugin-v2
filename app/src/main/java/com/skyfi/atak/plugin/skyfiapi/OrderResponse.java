package com.skyfi.atak.plugin.skyfiapi;

public class OrderResponse {
    private OrderRequest request;
    private int total;
    private Order[] orders;

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
        this.orders = orders;
    }

    public OrderRequest getRequest() {
        return request;
    }

    public void setRequest(OrderRequest request) {
        this.request = request;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
