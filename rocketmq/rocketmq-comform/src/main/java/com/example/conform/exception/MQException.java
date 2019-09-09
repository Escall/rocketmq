package com.example.conform.exception;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:RocketMQ的自定义异常
 */
public class MQException extends RuntimeException {
    public MQException(String msg)
    {
        super(msg);
    }
}
