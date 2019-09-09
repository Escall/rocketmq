package com.example.order.service;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
public interface OrderService {

    boolean createOrder(String cityId, String platformId, String userId,
                        String supplierId, String goodsId);

    void sendOrderlyMessage4Pkg(String userId, String orderId);
}
