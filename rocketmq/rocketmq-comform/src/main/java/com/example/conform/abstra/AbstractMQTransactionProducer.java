package com.example.conform.abstra;

import com.example.conform.exception.MQException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIME:2019/9/7
 * USER: EsCall
 * DESC: 事务生产者
 */
public abstract class AbstractMQTransactionProducer implements TransactionListener {
    private Logger                log = LoggerFactory.getLogger(AbstractMQTransactionProducer.class);
    private TransactionMQProducer transactionProducer;

    //需要在外面设置生产组名
    public void setProducer(TransactionMQProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    public SendResult sendMessageInTransaction(Message msg, Object arg) throws MQException {
        try {
            SendResult sendResult = transactionProducer.sendMessageInTransaction(msg, arg);
            if(sendResult.getSendStatus() != SendStatus.SEND_OK) {
                log.error("事务消息发送失败，topic : {}, msgObj {}", msg.getTopic(), msg);
                throw new MQException("事务消息发送失败，topic :" + msg.getTopic() + ", status :" + sendResult.getSendStatus());
            }
            log.info("发送事务消息成功，事务id: {}", msg.getTransactionId());
            return sendResult;
        } catch (Exception e) {
            log.error("事务消息发送失败，topic : {}, msgObj {}", msg.getTopic(), msg);
            throw new MQException("事务消息发送失败，topic :" + msg.getTopic() + ",e:" + e.getMessage());
        }
    }

}
