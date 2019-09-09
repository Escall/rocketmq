package com.example.store.service.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.store.service.HelloServiceApi;

/**
 * TIME:2019/9/3
 * USER: EsCall
 * DESC:dubbo服务实际提供者
 */
@Service(version="1.0.0",timeout = 3000)
public class HelloServiceProvider implements HelloServiceApi {
    @Override
    public String sayHello(String name) {
        System.out.println("hello wo:"+name);
        return "hello say";
    }
}
