package com.example.conform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:配置权限的数据
 */
@ConfigurationProperties(prefix = "spring.rocketmq")
public class MQProperties {

    private String nameServerAddress;

    private String producerGroup;

    private Integer sendMsgTimeout = 3000;
    //switch of trace message consumer: send message consumer info to topic: rmq_sys_TRACE_DATA
    private Boolean traceEnabled = Boolean.TRUE;

    private Boolean vipChannelEnabled = Boolean.TRUE;

    public String getNameServerAddress() {
        return nameServerAddress;
    }

    public void setNameServerAddress(String nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public Integer getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(Integer sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public Boolean getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(Boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public Boolean getVipChannelEnabled() {
        return vipChannelEnabled;
    }

    public void setVipChannelEnabled(Boolean vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }
}
