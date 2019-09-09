package com.example.conform.constrants;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC: 消费模式
 */
public enum ConsumeMode {
    //并发，事务消费
    CONCURRENTLY("CONCURRENTLY"),
    //顺序消费
    ORDERLY("ORDERLY");
    private String mode;

    ConsumeMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
