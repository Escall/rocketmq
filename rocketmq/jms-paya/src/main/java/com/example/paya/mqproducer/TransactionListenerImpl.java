package com.example.paya.mqproducer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.paya.mapping.CustomerAccountMapper;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TransactionListenerImpl implements TransactionListener {

    @Autowired
    CustomerAccountMapper customerAccountMapper;

    /**
     * message:消息
     * obj：你自定义的消息属性，例如：交易，下订单。。。或者传递给本地事务的一些参数数据
     * */
    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        System.out.println("执行本地事务...");
        //在这里进行数据库的操作
        CountDownLatch countDownLatch = null;
        Map<String, Object> map = (Map<String, Object>) o;
        try {
            String userId = (String) map.get("userId");
            String orderId = (String) map.get("orderId");
            String accountId = (String) map.get("accountId");
            BigDecimal payMoney = (BigDecimal) map.get("payMoney"); // 当前的支付款
            BigDecimal newBalance = (BigDecimal) map.get("newBalance");// 前面预扣款后的余额
            int currentVersion = (int) map.get("currVersion");
            countDownLatch = (CountDownLatch) map.get("countDownLatch");
            int count = customerAccountMapper.updateBalance(accountId,
                    newBalance, currentVersion, new Date());
            if (count == 1) {//扣款更新成功
                countDownLatch.countDown();
                System.out.println("事务执行完成");
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                countDownLatch.countDown();
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        } catch (Exception e) {

            e.printStackTrace();
            countDownLatch.countDown();
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

    }

    /***
     * 会查本地事务map，判断事务的状态如为0：UNKNOW，1：COMMIT_MESSAGE；
     * */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        System.out.println("进行事物回查。。。");
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
