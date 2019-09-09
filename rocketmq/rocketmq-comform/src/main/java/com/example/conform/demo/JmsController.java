package com.example.conform.demo;

import com.example.conform.message.MessageBuilder;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * TIME:2019/9/8
 * USER: EsCall
 * DESC:
 */
@RestController
public class JmsController {

    @Autowired
    DemoProducer demoProducer;

    @RequestMapping("/jms")
    public String jms(){
        Message msg = MessageBuilder.of("dajiahao!!!").topic("test_topic").tag("test").key(UUID.randomUUID().toString()).build();
        demoProducer.syncSend(msg);
        System.out.println("success");
        return "ddd";
    }

}
