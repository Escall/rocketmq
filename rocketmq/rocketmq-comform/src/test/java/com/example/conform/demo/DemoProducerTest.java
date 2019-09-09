package com.example.conform.demo;

import com.example.conform.message.MessageBuilder;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * TIME:2019/9/8
 * USER: EsCall
 * DESC:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DemoProducerTest {
    @Autowired
    DemoProducer producer;



    @Test
    public void send() {

        Message msg = MessageBuilder.of("dajiahao!!!").topic("test_topic").tag("test").key(UUID.randomUUID().toString()).build();
        producer.syncSend(msg);
        System.out.println("success");
    }
}