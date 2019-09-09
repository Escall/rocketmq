package com.example.order.mqconsumer;

import com.example.order.constants.OrderStatus;
import com.example.order.mapping.OrderMapper;
import com.example.order.service.OrderService;
import com.example.order.utils.FastJsonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * TIME:2019/9/5
 * USER: EsCall
 * DESC: 消费来自于callbackService发送过来的消息
 */
@Component
public class OrderConsumer {

    private      DefaultMQPushConsumer consumer;
    public final String                CONSUMER_GROUP_NAME = "callback_pay_consumer";
    public final String                CALLBACK_PAY_TOPIC  = "callback_pay_topic";

    public final String CALLBACK_PAY_TAGS = "callback_pay";

    public final String NAMESERVER = "10.25.8.121:9876";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    private OrderConsumer() throws Exception {
        this.consumer = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
        this.consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(20);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setNamesrvAddr(NAMESERVER);
        //订阅消息主题及类型
        consumer.subscribe(CALLBACK_PAY_TOPIC, CALLBACK_PAY_TAGS);
        //注册监听
        consumer.setMessageListener(new MessageListenerConcurrentlyFromPay());
        consumer.start();


    }

    /**
     * 监听消息并消费
     */
    class MessageListenerConcurrentlyFromPay implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            MessageExt messageExt = list.get(0);

            try {
                String topic = messageExt.getTopic();
                String tags = messageExt.getTags();
                String keys = messageExt.getKeys();
                String msgBody = new String(messageExt.getBody(), "utf-8");
                System.err.println("收到消息：" + "  topic :" + topic + "  ,tags : "
                        + tags + "keys :" + keys + ", msg : " + msgBody);
                Map<String ,Object> map= FastJsonUtil.convertJSONToObject(msgBody,Map.class);
                String orderId= (String) map.get("orderId");
                String userId= (String) map.get("userId");
                String status= (String) map.get("status");
                Date currentTime=new Date();
                if(status.equals(OrderStatus.ORDER_PAYED.getStatus())){//消费者已支付成功
                    //更新订单状态
                    int count=orderMapper.updateOrderStatus(
                            orderId, status, "admin",
                            currentTime);
                    if (count == 1) {
                        // 发起顺序消息
                        orderService.sendOrderlyMessage4Pkg(
                                userId, orderId);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;//稍后重试
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


}
