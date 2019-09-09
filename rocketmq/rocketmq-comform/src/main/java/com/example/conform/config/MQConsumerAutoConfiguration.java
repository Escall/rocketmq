package com.example.conform.config;

import com.example.conform.abstra.AbstractMQConsumer;
import com.example.conform.abstra.AbstractMQPushConsumer;
import com.example.conform.annotation.MQConsumer;
import com.example.conform.constrants.MessageExtConst;
import com.example.conform.exception.MQException;
import com.example.conform.traces.common.OnsTraceConstants;
import com.example.conform.traces.dispatch.impl.AsyncTraceAppender;
import com.example.conform.traces.dispatch.impl.AsyncTraceDispatcher;
import com.example.conform.traces.tracehook.OnsConsumeMessageHookImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:自动装配消息消费者
 */
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQConsumerAutoConfiguration extends MQBaseAutoConfiguration {
    protected Logger log = LoggerFactory.getLogger(MQConsumerAutoConfiguration.class);

    private AsyncTraceDispatcher asyncTraceDispatcher;
    // 维护一份map用于检测是否用同样的consumerGroup订阅了不同的topic+tag
    private Map<String, String>  validConsumerMap;

    @PostConstruct//先执行构造方法,再执行被PostConstruct修饰的方法
    //在对象的初始化和依赖都完成之后才会执行，在init方法之后执行的才是afterPropertiesSet方法，这个方法必须实现InitializingBean接口
    public void init() throws Exception {
        //找到mqconsumer
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQConsumer.class);
        if(!CollectionUtils.isEmpty(beans) && properties.getTraceEnabled()) {
            initAsyncAppender();//轨迹追踪
        }
        validConsumerMap=new HashMap<>();
        for(Map.Entry<String,Object> entry:beans.entrySet()){
            publishConsumer(entry.getKey(), entry.getValue());//广播消费者
        }
        // 清空map，等待回收
        validConsumerMap = null;
    }

    private void publishConsumer(String key, Object bean) throws Exception{
        MQConsumer mqConsumer=applicationContext.findAnnotationOnBean(key,MQConsumer.class);
        if (StringUtils.isEmpty(properties.getNameServerAddress())) {
            throw new MQException("name server address must be defined");
        }
        Assert.notNull(mqConsumer.consumerGroup(), "consumer's consumerGroup must be defined");
        Assert.notNull(mqConsumer.topic(), "consumer's topic must be defined");
        if (!AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
            throw new MQException(bean.getClass().getName() + " - consumer未实现Consumer抽象类");
        }
        //得到配置环境
        Environment environment = applicationContext.getEnvironment();
        //得到配置的组名，topic，tags
        String consumerGroup = environment.resolvePlaceholders(mqConsumer.consumerGroup());
        String topic = environment.resolvePlaceholders(mqConsumer.topic());
        String tags = "*";
        if(mqConsumer.tag().length == 1) {
            tags = environment.resolvePlaceholders(mqConsumer.tag()[0]);
        } else if(mqConsumer.tag().length > 1) {
            tags = StringUtils.join(mqConsumer.tag(), "||");
        }
        // 检查consumerGroup是否重复订阅
        if(!StringUtils.isEmpty(validConsumerMap.get(consumerGroup))) {
            String exist = validConsumerMap.get(consumerGroup);
            throw new RuntimeException("消费组重复订阅，请新增消费组用于新的topic和tag组合: " + consumerGroup + "已经订阅了" + exist);
        } else {
            validConsumerMap.put(consumerGroup, topic + "-" + tags);
        }
        // 配置push consumer
        if(AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())){//abs是bean的父类或者同类
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setNamesrvAddr(properties.getNameServerAddress());
            consumer.setMessageModel(MessageModel.valueOf(mqConsumer.messageMode()));
            consumer.subscribe(topic, tags);//订阅
            consumer.setInstanceName(UUID.randomUUID().toString());//客户端名称
            consumer.setVipChannelEnabled(properties.getVipChannelEnabled());
            AbstractMQPushConsumer abstractMQPushConsumer = (AbstractMQPushConsumer) bean;
            if (MessageExtConst.CONSUME_MODE_CONCURRENTLY.equals(mqConsumer.consumeMode())) {//消费模式
                consumer.registerMessageListener((List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeConcurrentlyContext));
            } else if (MessageExtConst.CONSUME_MODE_ORDERLY.equals(mqConsumer.consumeMode())) {//顺序消费
                consumer.registerMessageListener((List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeOrderlyContext));
            } else {
                throw new MQException("unknown consume mode ! only support CONCURRENTLY and ORDERLY");
            }
            abstractMQPushConsumer.setConsumer(consumer);

            // 为Consumer增加消息轨迹回发模块
            if (properties.getTraceEnabled()) {
                try {
                    consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
                            new OnsConsumeMessageHookImpl(asyncTraceDispatcher));
                } catch (Throwable e) {
                    log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
                }
            }

            consumer.start();
        }

        log.info(String.format("%s is ready to subscribe message", bean.getClass().getName()));


    }

    private AsyncTraceDispatcher initAsyncAppender() {
        if(asyncTraceDispatcher != null) {
            return asyncTraceDispatcher;
        }
        try {
            Properties tempProperties = new Properties();
            tempProperties.put(OnsTraceConstants.MaxMsgSize, "128000");
            tempProperties.put(OnsTraceConstants.AsyncBufferSize, "2048");
            tempProperties.put(OnsTraceConstants.MaxBatchNum, "1");
            tempProperties.put(OnsTraceConstants.WakeUpNum, "1");
            tempProperties.put(OnsTraceConstants.NAMESRV_ADDR, properties.getNameServerAddress());
            tempProperties.put(OnsTraceConstants.InstanceName, UUID.randomUUID().toString());
            AsyncTraceAppender asyncAppender = new AsyncTraceAppender(tempProperties);
            asyncTraceDispatcher = new AsyncTraceDispatcher(tempProperties);
            asyncTraceDispatcher.start(asyncAppender, "DEFAULT_WORKER_NAME");
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return asyncTraceDispatcher;
    }

}
