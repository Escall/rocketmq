package com.example.conform;

import com.example.conform.annotation.MQConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:
 */
@SpringBootApplication
@MQConfiguration
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
