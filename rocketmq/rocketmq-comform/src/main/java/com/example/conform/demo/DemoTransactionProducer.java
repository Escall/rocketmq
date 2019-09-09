package com.example.conform.demo;

import com.example.conform.abstra.AbstractMQTransactionProducer;
import com.example.conform.annotation.MQTransactionProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * TIME:2019/9/8
 * USER: EsCall
 * DESC:
 */
//@MQTransactionProducer(producerGroup = "tx_demo_producer")
public class DemoTransactionProducer extends AbstractMQTransactionProducer {
    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        return null;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        return null;
    }
}
