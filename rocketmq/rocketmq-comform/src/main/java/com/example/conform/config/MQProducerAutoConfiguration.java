package com.example.conform.config;

import com.example.conform.abstra.AbstractMQTransactionProducer;
import com.example.conform.annotation.MQProducer;
import com.example.conform.annotation.MQTransactionProducer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TIME:2019/9/7
 * USER: EsCall
 * DESC: 生产者装配
 */
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)//条件装配
public class MQProducerAutoConfiguration extends MQBaseAutoConfiguration {
    protected Logger log = LoggerFactory.getLogger(MQProducerAutoConfiguration.class);

    private static DefaultMQProducer producer;

    public static void setProducer(DefaultMQProducer producer) {
        MQProducerAutoConfiguration.producer = producer;
    }

    @Bean
    public DefaultMQProducer exposeProducer() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQProducer.class);
        //对于仅仅只存在消息消费者的项目，无需构建生产者
        if (CollectionUtils.isEmpty(beans)) {
            return null;
        }
        if (producer == null) {
            Assert.notNull(properties.getProducerGroup(), "producer group must be defined");
            Assert.notNull(properties.getNameServerAddress(), "name server address must be defined");
            producer = new DefaultMQProducer(properties.getProducerGroup());
            producer.setNamesrvAddr(properties.getNameServerAddress());
            producer.setSendMsgTimeout(properties.getSendMsgTimeout());
            producer.setSendMessageWithVIPChannel(properties.getVipChannelEnabled());
            producer.start();
        }
        return producer;
    }

    @PostConstruct
    public void configTransactionProducer() {
        Map<String,Object> beans=applicationContext.getBeansWithAnnotation(MQTransactionProducer.class);
        if(CollectionUtils.isEmpty(beans))
            return;
        ExecutorService executorService=new ThreadPoolExecutor(beans.size(), beans.size() * 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30), new ThreadFactory() {
           AtomicInteger integer=new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread=new Thread(r);
                thread.setName("tx_check$"+integer.getAndIncrement());
                return thread;
            }
        });
        Environment environment=applicationContext.getEnvironment();
        //配置transproducer
        beans.entrySet().forEach(transactionProducer->{
            try {
                //get
                AbstractMQTransactionProducer bean=AbstractMQTransactionProducer.class.cast(transactionProducer.getValue());
                MQTransactionProducer anno=bean.getClass().getAnnotation(MQTransactionProducer.class);
                //赋值组名
                TransactionMQProducer producer=new TransactionMQProducer(environment.resolvePlaceholders(anno.producerGroup()));
                producer.setNamesrvAddr(properties.getNameServerAddress());
                producer.setExecutorService(executorService);
                producer.setTransactionListener(bean);
                producer.start();
                bean.setProducer(producer);
            } catch (MQClientException e) {
                log.error("build transaction producer error : {}", e);
            }
        });
    }
}
