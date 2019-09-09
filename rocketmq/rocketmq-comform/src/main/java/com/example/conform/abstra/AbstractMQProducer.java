package com.example.conform.abstra;


import com.example.conform.exception.MQException;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * TIME:2019/9/7
 * USER: EsCall
 * DESC:生产者
 */
public abstract  class AbstractMQProducer<T> {
    protected  Logger               log      = LoggerFactory.getLogger(AbstractMQProducer.class);
    private          MessageQueueSelector selector = new SelectMessageQueueByHash();
    @Autowired
    private          DefaultMQProducer    producer;
   public AbstractMQProducer() {
    }
    /**
     *@Description 同步发送消息
     *@Param [message]
     *@Return void
     */
    public void syncSend(Message message) throws MQException {
        try {
            SendResult sendResult = producer.send(message);
            log.debug("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(message, sendResult);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", message.getTopic(), message);
            throw new MQException("消息发送失败，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }
    /**
     *@Description 重写，处理发送后的逻辑
     *@Param [message, sendResult]
     *@Return void
     */
    private void doAfterSyncSend(Message message, SendResult sendResult) {

    }
    /**
     * 异步发送消息
     * @param message msgObj
     * @param sendCallback 回调
     * @throws MQException 消息异常
     */
    public void asyncSend(Message message, SendCallback sendCallback) throws MQException {
        try {
            producer.send(message, sendCallback);
            log.debug("send rocketmq message async");
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", message.getTopic(), message);
            throw new MQException("消息发送失败，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }

}
