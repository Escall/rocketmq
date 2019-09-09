package com.example.paya.mqproducer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.paya.utils.FastJsonUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/****
 *
 * 回调告诉订单系统支付成功，通知他修改订单状态
 *
 */

@Service
public class CallbackService {

    public static final String CALLBACK_PAY_TOPIC = "callback_pay_topic";

    public static final String CALLBACK_PAY_TAGS = "callback_pay";


    @Autowired
    private SyncProducer syncProducer;

    public void sendOKMessage(String orderId, String userId) {

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("orderId", orderId);
        params.put("status", "2"); // ok,订单支付成功

        String keys = UUID.randomUUID().toString() + "$"
                + System.currentTimeMillis();
        Message message = new Message(CALLBACK_PAY_TOPIC, CALLBACK_PAY_TAGS,
                keys,
                FastJsonUtil.convertObjectToJSON(params).getBytes());
         syncProducer.sendMessage(message);

    }

}
