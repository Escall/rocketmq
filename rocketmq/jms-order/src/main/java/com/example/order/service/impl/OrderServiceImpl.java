package com.example.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.order.constants.OrderStatus;
import com.example.order.domain.Order;
import com.example.order.mapping.OrderMapper;
import com.example.order.mqproducer.OrderlyProducer;
import com.example.order.service.OrderService;
import com.example.order.utils.FastJsonUtil;
import com.example.store.service.StoreSerivceApi;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC: 下订单服务
 */
@Service//会被注册到ioc中
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper     orderMapper;
    @Reference(version = "1.0.0", application = "${dubbo.application.id}",
            interfaceName = "com.example.store.service.StoreServiceApi", timeout = 3000)
    private StoreSerivceApi storeSerivceApi;

    @Override
    public boolean createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId) {
        boolean flag = false;

        try {
            //生成订单逻辑
            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString().substring(0, 32));// subString为左闭右开区间

            order.setOrderType("1");// 订单类型
            order.setCityId(cityId);
            order.setPlatformId(platformId);
            order.setUserId(userId);
            order.setSupplierId(supplierId);
            order.setGoodsId(goodsId);
            order.setOrderStatus(OrderStatus.ORDER_CREATED.getStatus());
            order.setRemark("下单成功");
            order.setCreateBy("admin");

            Date currentTime = new Date();
            order.setCreateTime(currentTime);
            order.setUpdateBy("admin");
            order.setUpdateTime(currentTime);
            // 注意：满足库存条件时，才能下单成功
//            long result = orderMapper.insertSelective(order);
            //查询版本号来更新库存
            int version = storeSerivceApi.selectVersion(supplierId, goodsId);
            int updateCount = storeSerivceApi.updateStoreCountByVersion(version, supplierId, goodsId, "admin", new Date());
            if (updateCount == 1) {
                orderMapper.insertSelective(order);//若在此处出现sql异常，则在cacth中捕获并回滚事件
                flag = true;
            } else if (updateCount == 0) {//未更新成功
                //原因两条：乐观锁生效；库存不足
                //查询库存
                int storeCount = storeSerivceApi.selectStoreCount(supplierId, goodsId);
                if (storeCount == 0)
                    System.out.println("当前库存不足");

            } else
                //有可能两个线程带着相同的版本号来更新，以至失败
                System.out.println("乐观锁生效。。。。");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;

    }
    public static final String PKG_TOPIC = "pkg_topic";

    public static final String          PKG_TAGS = "pkg";
    @Autowired
    private             OrderlyProducer orderlyProducer;
    // 发送顺序消息给pkg服务
    @Override
    public void sendOrderlyMessage4Pkg(String userId, String orderId) {
        List<Message> messageList = new ArrayList<>();

        Map<String, Object> param1 = new HashMap<String, Object>();
        param1.put("userId", userId);
        param1.put("orderId", orderId);// 全局唯一，独一无二
        param1.put("text", "创建包裹操作---1");

        String key1 = UUID.randomUUID().toString() + "$"
                + System.currentTimeMillis();
        Message message1 = new Message(PKG_TOPIC, PKG_TAGS, key1,
                FastJsonUtil.convertObjectToJSON(param1).getBytes());

        messageList.add(message1);

        Map<String, Object> param2 = new HashMap<String, Object>();
        param2.put("userId", userId);
        param2.put("orderId", orderId);
        param2.put("text", "发送物流通知操作---2");

        String key2 = UUID.randomUUID().toString() + "$"
                + System.currentTimeMillis();
        Message message2 = new Message(PKG_TOPIC, PKG_TAGS, key2,
                FastJsonUtil.convertObjectToJSON(param2).getBytes());

        messageList.add(message2);

        // 顺序消息投递 是应该按照 供应商ID 与topic 和 messagequeueId 进行绑定，一一对应的

        // 找到 supplier_id
        Order order = orderMapper.selectByPrimaryKey(orderId);
        int messageQueueNumber = Integer.parseInt(order.getSupplierId());

        // 对应的顺序消息的生产者 把messageList 发出去
       orderlyProducer.sendOrderlyMessages(messageList, messageQueueNumber);
    }
}
