package com.example.order.service.impl;

import com.example.order.App;
import com.example.order.constants.OrderStatus;
import com.example.order.domain.Order;
import com.example.order.mapping.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@Transactional //支持数据回滚，避免测试数据污染环境
@RunWith(SpringRunner.class)
@SpringBootTest()
public class OrderServiceImplTest {
    @Autowired
    OrderMapper orderMapper;

    @Test
    public void createOrder() {


        try {
            //生成订单逻辑
            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString().substring(0, 10));// subString为左闭右开区间

            order.setOrderType("1");// 订单类型
            order.setCityId("11");
            order.setPlatformId("3");
            order.setUserId("12345");
            order.setSupplierId("0");
            order.setGoodsId("23");
            order.setOrderStatus(OrderStatus.ORDER_CREATED.getStatus());
            order.setRemark("下单成功");
            order.setCreateBy("admin");

            Date currentTime = new Date();
            order.setCreateTime(currentTime);
            order.setUpdateBy("admin");
            order.setUpdateTime(currentTime);

            long result = orderMapper.insertSelective(order);
            System.out.println("返回结果为"+result+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}