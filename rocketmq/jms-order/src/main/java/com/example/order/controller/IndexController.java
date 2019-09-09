package com.example.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.store.service.HelloServiceApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TIME:2019/9/3
 * USER: EsCall
 * DESC:
 */
@RestController
public class IndexController {
    @RequestMapping("/index")
    public String index() throws Exception{
        System.out.println("index");
        return "index !";
    }

    /**
     * 重要：这是dubbo的消费者，利用reference来引用
     * */
    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            interfaceName = "com.example.store.service.HelloServiceApi",
            timeout = 3000,check = false,retries = 3)//读请求可重复，写不可
    private HelloServiceApi helloServiceApi;
    @RequestMapping("/hello")
    public String hello(@RequestParam("name") String name){
        helloServiceApi.sayHello(name);
        return "say hello  !!!!"+name;
    }
}
