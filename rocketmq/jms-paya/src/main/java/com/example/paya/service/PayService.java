package com.example.paya.service;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
public interface PayService {
    String payment(String userId, String orderId,String accountId,double money);
}
