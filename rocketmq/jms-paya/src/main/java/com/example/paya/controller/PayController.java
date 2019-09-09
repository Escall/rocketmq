package com.example.paya.controller;

import com.example.paya.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TIME:2019/9/5
 * USER: EsCall
 * DESC:
 */
@RestController
public class PayController {

    @Autowired
    PayService payService;

    @RequestMapping("/paya")
    public String pay(@RequestParam("userId") String userId,
                      @RequestParam("orderId") String orderId,
                      @RequestParam("accountId") String accountId,
                      @RequestParam("money") double money) {
        // 支付第一步 ，提交
        return payService.payment(userId, orderId, accountId, money);

    }
}
