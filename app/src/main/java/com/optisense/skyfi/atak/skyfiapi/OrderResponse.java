package com.optisense.skyfi.atak.skyfiapi;

import java.util.Arrays;

public class OrderResponse {
    private OrderRequest request;
    private int total;
    private Order[] orders;

    public Order[] getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "orders=" + Arrays.toString(orders) +
                ", request=" + request +
                ", total=" + total +
                '}';
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
