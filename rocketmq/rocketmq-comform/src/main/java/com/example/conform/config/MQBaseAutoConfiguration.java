package com.example.conform.config;

import com.example.conform.abstra.AbstractMQProducer;
import com.example.conform.abstra.AbstractMQPushConsumer;
import com.example.conform.annotation.MQConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC: RocketMQ配置文件
 */
@Configuration
@ConditionalOnBean(annotation = MQConfiguration.class)//（在ioc中存在某个对象时，才会实例化一个Bean）
//加载配置的类之后再加载当前类
@AutoConfigureAfter({AbstractMQProducer.class, AbstractMQPushConsumer.class})
/**
 * 如果一个配置类只配置@ConfigurationProperties注解，而没有使用@Component，
 * 那么在IOC容器中是获取不到properties 配置文件转化的bean。
 * 说白了 @EnableConfigurationProperties
 * 相当于把使用 @ConfigurationProperties 的类进行了一次注入。
 * */
@EnableConfigurationProperties(MQProperties.class)
public class MQBaseAutoConfiguration implements ApplicationContextAware {
    protected  MQProperties                  properties;
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    public void setMqProperties(MQProperties mqProperties) {
        this.properties = mqProperties;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
