package com.example.order.constants;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC: 订单状态
 */
public enum OrderStatus {

    ORDER_CREATED("1"),
    ORDER_PAYED("2"),
    ORDER_FAIL("3");
    private String status;

   OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
