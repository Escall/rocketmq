package com.example.conform.demo;

import com.example.conform.abstra.AbstractMQPushConsumer;
import com.example.conform.annotation.MQConsumer;

import java.util.Map;

/**
 * TIME:2019/9/8
 * USER: EsCall
 * DESC:
 */
@MQConsumer(topic = "test_topic",tag = "*",consumerGroup = "local_consumer_conform")
public class DemoConsumer extends AbstractMQPushConsumer {

    @Override
    public boolean process(Object message, Map extMap) {
        // extMap 中包含messageExt中的属性和message.properties中的属性
        System.out.println(message);
        System.out.println("接收消息成功");
        return true;
    }
}
