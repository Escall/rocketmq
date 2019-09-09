package com.example.order.controller;

import com.example.order.service.OrderService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@RestController
public class OrderController {


    @Autowired
    private OrderService orderService;

    @HystrixCommand(
            commandKey = "createOrder", // 一般和你的请求路径相同
            commandProperties = {
                    @HystrixProperty(
                            name = "execution.timeout.enabled",
                            value = "true"),//执行超时策略
                    @HystrixProperty(
                            name = "execution.isolation.thread.timeoutInMilliseconds",
                            value = "3000")},
            // 若超时则降级,注意降级方法和原方法参数返回类型要一致，相当于替代品
            fallbackMethod = "createOrderFallbackMethod3Timeout")
    @RequestMapping("/createOrder")
    public String createOrder(@RequestParam("cityId") String cityId,
                              @RequestParam("platformId") String platformId,
                              @RequestParam("userId") String userId,
                              @RequestParam("supplierId") String supplierId,
                              @RequestParam("goodsId") String goodsId) throws Exception {
        // 10.5对该请求降级，限流操作
        // Thread.sleep(5000);

        return orderService.createOrder(cityId, platformId, userId, supplierId,
                goodsId) ? "下单成功!" : "下单失败！";
    }

    public String createOrderFallbackMethod3Timeout(
            @RequestParam("cityId") String cityId,
            @RequestParam("platformId") String platformId,
            @RequestParam("userId") String userId,
            @RequestParam("supplierId") String supplierId,
            @RequestParam("goodsId") String goodsId) throws Exception {

        System.out.println("执行超时降级操作策略");
        return "hystrix timeout !";
    }







    // hystrix采用线程池策略限流
    @HystrixCommand(
            commandKey = "CreateOrder",
            commandProperties = {@HystrixProperty(
                    name = "execution.isolation.strategy",
                    value = "THREAD"/** 需制定threadpoolkey */
            ),},
            threadPoolKey = "CreateOrderThreadPool",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "10"),
                    @HystrixProperty(name = "macQueueSize", value = "1000"), // 数值大一些以便修改下面的配置
                    @HystrixProperty(
                            name = "queueSizeRejectionThreshold",
                            value = "30")// 来41个请求，前十个进入core，30个进入队列，第41个拒绝
            },
            // 若超时则降级,注意降级方法和原方法参数返回类型要一致，相当于替代品
            fallbackMethod = "CreateOrderFallbackMethod3Timeout")
    @RequestMapping("/CreateOrder")
    public String CreateOrder(@RequestParam("cityId") String cityId,
                              @RequestParam("platformId") String platformId,
                              @RequestParam("userId") String userId,
                              @RequestParam("supplierId") String supplierId,
                              @RequestParam("goodsId") String goodsId) throws Exception {

        // 10.5对该请求降级，限流操作
        Thread.sleep(5000);
        return "下单成功!";
    }

    public String CreateOrderFallbackMethod3Timeout(
            @RequestParam("cityId") String cityId,
            @RequestParam("platformId") String platformId,
            @RequestParam("userId") String userId,
            @RequestParam("supplierId") String supplierId,
            @RequestParam("goodsId") String goodsId) throws Exception {

        System.out.println("执行线程池限流降级操作策略");
        return "hystrix threadpool !";
    }

    // hystrix采用信号量策略限流:同一时间允许3个请求
    @HystrixCommand(
            commandKey = "semaphoerCreateOrder",
            commandProperties = {@HystrixProperty(
                    name = "execution.isolation.strategy",
                    value = "SEMAPHORE"/** 需制定threadpoolkey */
            ), @HystrixProperty(
                    name = "execution.isolation.semaphore.maxConcurrentRequests",
                    value = "3")},

            // 若超时则降级,注意降级方法和原方法参数返回类型要一致，相当于替代品
            fallbackMethod = "semaphoreCreateOrderFallbackMethod3Timeout")
    @RequestMapping("/semaphoreCreateOrder")
    public String semaphoreCreateOrder(@RequestParam("cityId") String cityId,
                                       @RequestParam("platformId") String platformId,
                                       @RequestParam("userId") String userId,
                                       @RequestParam("supplierId") String supplierId,
                                       @RequestParam("goodsId") String goodsId) throws Exception {

        return "下单成功!";
    }

    public String semaphoreCreateOrderFallbackMethod3Timeout(
            @RequestParam("cityId") String cityId,
            @RequestParam("platformId") String platformId,
            @RequestParam("userId") String userId,
            @RequestParam("supplierId") String supplierId,
            @RequestParam("goodsId") String goodsId) throws Exception {

        System.out.println("执行信号量限流降级操作策略");
        return "hystrix semaphore !";
    }

}


