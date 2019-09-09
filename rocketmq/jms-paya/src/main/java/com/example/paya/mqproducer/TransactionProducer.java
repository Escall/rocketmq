package com.example.paya.mqproducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * 消息生产者，业务要单一
 * */
@Component
public class TransactionProducer implements InitializingBean {

    private TransactionMQProducer   producer;
    @Autowired
    private TransactionListenerImpl listener;

    private ExecutorService executorService;

    private String PRODUCER_GROUP_NAME = "tx_pay_producer_group_name";
    private static final String NAMESRV       = "10.25.8.121:9876";

    private TransactionProducer() {
        this.producer = new TransactionMQProducer(PRODUCER_GROUP_NAME);
        executorService = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread();
                thread.setName("transaction-check-thread$" + mCount.getAndIncrement());
                return thread;
            }
        }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("当前任务太多，请稍后再试！");
            }
        });
        producer.setExecutorService(executorService);
        producer.setNamesrvAddr(NAMESRV);


    }

    public void start() {
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        this.producer.shutdown();
    }

    /**
     * 在初始化时，当listener注入进来后，再执行该方法中语句
     * 即准备工作完成后，执行方法中的语句
     **/
    @Override
    public void afterPropertiesSet()  {
        producer.setTransactionListener(listener);
        start();
    }

    /**
     * 回调参数可用与消息确认，再判断之类的
     */
    public TransactionSendResult sendMessage(Message msg, Object obj) {
        TransactionSendResult result = null;
        try {
            result = producer.sendMessageInTransaction(msg, obj);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return result;
    }
}
