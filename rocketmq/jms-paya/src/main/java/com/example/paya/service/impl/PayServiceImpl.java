package com.example.paya.service.impl;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.example.paya.domain.CustomerAccount;
import com.example.paya.mapping.CustomerAccountMapper;
import com.example.paya.service.PayService;
import com.example.paya.mqproducer.CallbackService;
import com.example.paya.mqproducer.TransactionProducer;
import com.example.paya.utils.FastJsonUtil;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@Service
public class PayServiceImpl implements PayService {

    public static final String                TX_PAY_TOPIC = "tx_pay_topic";
    public static final String                TX_PAY_TAG   = "tx_pay";
    @Autowired
    private             CustomerAccountMapper customerAccountMapper;
    @Autowired
    private             TransactionProducer   transactionProducer;
    @Autowired
    private             CallbackService       callbackService;

    @Override
    public String payment(String userId, String orderId, String accountId,
                          double money) {

        String payResult = "";
        try {
            // 最开始应该有一步token验证操作

            BigDecimal payMoney = new BigDecimal(money);
            // 先余额查询
            CustomerAccount oldAccount = customerAccountMapper
                    .selectByPrimaryKey(accountId);
            BigDecimal currentBalance = oldAccount.getCurrentBalance();//当前余额
            int currVersion = oldAccount.getVersion();
            // 预减钱，以判断余额是否充足
            // 对大概率事件进行提前预判，对小概率事件进行兜底,最后保障数据一致性，简单的加锁
            BigDecimal newBalance = currentBalance.subtract(payMoney);
            if (newBalance.doubleValue() > 0) {
                // 若钱满足条件，够减的，则做两件事
                // 1.组装事务消息
                // 2.并行执行本地减钱操作
                String key = orderId + "$"
                        + System.currentTimeMillis();// 生成该消息的唯一key
                Map<String, Object> map = new HashMap<>();
                map.put("userId", userId);
                map.put("orderId", orderId);
                map.put("accountId", accountId);
                map.put("money", money);
                Message message = new Message(TX_PAY_TOPIC, TX_PAY_TAG, key,
                        FastJsonUtil.convertObjectToJSON(map)
                                .getBytes());
                // 执行本地事务可能需要用到的参数
                map.put("payMoney", payMoney);
                map.put("newBalance", newBalance);
                map.put("currVersion", currVersion);
                // 同步阻塞
                CountDownLatch countDownLatch = new CountDownLatch(1);
                map.put("countDownLatch", countDownLatch);
                // 支付第二步，执行，有两件事 “消事”
                // 发送给——payb的consumer消息并且执行本地事务
                TransactionSendResult sendResult = transactionProducer
                        .sendMessage(message, map);

                countDownLatch.await();
                //消息发送成功并且本地事务执行成功
                if (sendResult.getSendStatus() == SendStatus.SEND_OK
                        && sendResult
                        .getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
                    // 支付第三步，回调： 回调callback，通知OrderConsumer支付成功消息,更改订单状态
                    callbackService.sendOKMessage(orderId, userId);
                    payResult = "支付成功！";

                } else {
                    payResult = "支付失败！";
                }
            } else {
                payResult = "余额不足！";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return payResult;
    }

}