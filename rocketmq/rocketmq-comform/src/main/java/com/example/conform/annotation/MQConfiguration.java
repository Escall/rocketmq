package com.example.conform.annotation;

import java.lang.annotation.*;

/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:rocketmq 配置注解
 * 注解的本质就是一个继承了 Annotation 接口的接口
 *
 * @Target：注解的作用目标
 * @Retention：注解的生命周期
 * @Documented：注解是否应当被包含在 JavaDoc 文档中
 * @Inherited：是否允许子类继承该注解
 */
@Target(ElementType.TYPE)//用于描述注解的使用范围:类、接口(包括注解类型) 或enum声明
@Retention(RetentionPolicy.RUNTIME)//注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
@Documented//注释是由javadoc记录，注释成为公共API的一部分
@Inherited//用于标注一个父类的注解是否可以被子类继承
public @interface MQConfiguration {

}
