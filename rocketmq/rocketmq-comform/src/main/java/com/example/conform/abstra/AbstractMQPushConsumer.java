package com.example.conform.abstra;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;

/**
 * TIME:2019/9/7
 * USER: EsCall
 * DESC: 消费者(Push模式)处理消息的接口,应用中多使用此接口
 */
public abstract class AbstractMQPushConsumer<T>  extends AbstractMQConsumer<T> {

    private DefaultMQPushConsumer consumer;
    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DefaultMQPushConsumer consumer) {
        this.consumer = consumer;
    }

   public AbstractMQPushConsumer(){

    }
    /**
     * 继承这个方法处理消息
     * @param message 消息范型m
     * @param map 存放消息附加属性的map, map中的key存放在 @link MessageExtConst 中
     * @return 处理结果
     */
    public abstract boolean process(T message, Map<String,Object>map);

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     * 多线程消费
     * @param list 消息列表
     * @param consumeConcurrentlyContext 上下文
     * @return 消费状态
     */
    public ConsumeConcurrentlyStatus dealMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for(MessageExt messageExt : list) {
            log.debug("receive msgId: {}, tags : {}" , messageExt.getMsgId(), messageExt.getTags());
            // parse message body
            T t = parseMessage(messageExt);
            // parse ext properties附加属性
            Map<String, Object> ext = parseExtParam(messageExt);
            if( null != t && !process(t, ext)) {
                log.warn("consume fail , ask for re-consume , msgId: {}", messageExt.getMsgId());
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return  ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }


    /**
     *        顺序消费
     * @param list 消息列表
     * @param consumeOrderlyContext 上下文
     * @return 处理结果
     */
    public ConsumeOrderlyStatus dealMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        for(MessageExt messageExt : list) {
            log.info("receive msgId: {}, tags : {}" , messageExt.getMsgId(), messageExt.getTags());
            T t = parseMessage(messageExt);
            Map<String, Object> ext = parseExtParam(messageExt);
            if( null != t && !process(t, ext)) {
                log.warn("consume fail , ask for re-consume , msgId: {}", messageExt.getMsgId());
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
        return  ConsumeOrderlyStatus.SUCCESS;
    }

}
