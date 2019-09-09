package com.example.payb.mqconsumer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.example.payb.domain.PlatformAccount;
import com.example.payb.mapping.PlatformAccountMapper;
import com.example.payb.utils.FastJsonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订阅事务消息，平台帐号加钱
 * */
@Component
public class PayConsumer implements InitializingBean {

    private DefaultMQPushConsumer consumer;

    private static final String NAMESERVER = "10.25.8.121:9876";

    private static final String CONSUMER_GROUP_NAME = "tx_pay_consumer_group_name";

    public static final String TX_PAY_TOPIC = "tx_pay_topic";

    public static final String TX_PAY_TAGS = "tx_pay";

    @Autowired
    private PlatformAccountMapper platformAccountMapper;

    private PayConsumer() {
        try {
            this.consumer = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
            this.consumer.setConsumeThreadMin(10);
            this.consumer.setConsumeThreadMax(20);
            this.consumer.setNamesrvAddr(NAMESERVER);
            this.consumer.setConsumeFromWhere(
                    ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            this.consumer.subscribe(TX_PAY_TOPIC, TX_PAY_TAGS);

        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.consumer.registerMessageListener(
                new MessageListenerConcurrently4Pay());
        this.consumer.start();
        System.out.println("pay consumer start...");
    }

    class MessageListenerConcurrently4Pay
            implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                        ConsumeConcurrentlyContext context) {
            MessageExt msg = msgs.get(0);
            try {
                String topic = msg.getTopic();
                String tags = msg.getTags();
                String keys = msg.getKeys();
                String body = new String(msg.getBody(),
                        RemotingHelper.DEFAULT_CHARSET);
                System.err.println("收到事务消息, topic: " + topic + ", tags: " + tags
                        + ", keys: " + keys + ", body: " + body);

                Map<String, Object> paramsBody = FastJsonUtil
                        .convertJSONToObject(body, Map.class);
                String userId = (String) paramsBody.get("userId");
                String accountId = (String) paramsBody.get("accountId");
                String orderId = (String) paramsBody.get("orderId"); // 统一的订单
                BigDecimal money = (BigDecimal) paramsBody.get("money"); // 当前的收益款
                //获取平台账号信息 ,
                PlatformAccount pa = platformAccountMapper
                        .selectByPrimaryKey("platform001"); // 当前平台的一个账号
                pa.setCurrentBalance(pa.getCurrentBalance().add(money));
                Date currentTime = new Date();
                pa.setVersion(pa.getVersion() + 1);
                pa.setDateTime(currentTime);
                pa.setUpdateTime(currentTime);
                platformAccountMapper.updateByPrimaryKeySelective(pa);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("消息失败重试次数："+msg.getReconsumeTimes());
                // 如果处理多次操作还是失败, 记录失败日志（做补偿 回顾 人工处理）
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

    }

}
