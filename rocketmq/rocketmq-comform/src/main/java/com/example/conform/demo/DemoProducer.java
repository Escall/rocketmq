package com.example.conform.demo;

import com.example.conform.abstra.AbstractMQProducer;
import com.example.conform.annotation.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * TIME:2019/9/8
 * USER: EsCall
 * DESC:
 */
@MQProducer
public class DemoProducer extends AbstractMQProducer {
    /**
     *@Description 重写，处理发送后的逻辑
     *@Param [message, sendResult]
     *@Return void
     */
    private void doAfterSyncSend(Message message, SendResult sendResult) {
        System.out.println("message send successfully!");
    }
}
