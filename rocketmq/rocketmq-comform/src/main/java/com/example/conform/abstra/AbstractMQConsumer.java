package com.example.conform.abstra;

import com.example.conform.constrants.MessageExtConst;
import com.example.conform.utils.FastJsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * TIME:2019/9/7
 * USER: EsCall
 * DESC:消费者
 */

public abstract class AbstractMQConsumer<T> {
    protected   Logger log = LoggerFactory.getLogger(AbstractMQConsumer.class);

    protected Gson gson = new Gson();

/**
 *
*@Description 解析消息
*@Param
*@Return
*/
    protected T parseMessage(MessageExt message) {
        if (message == null || message.getBody() == null)
            return null;
        final Type type = this.getMessageType();
        if (type instanceof Class) {
            try {
                T data = gson.fromJson(new String(message.getBody()), type);
//                T data= FastJsonUtil.convertJSONToObject(new String(message.getBody()),(Class<T>) type);
                return data;
            } catch (JsonSyntaxException e) {
                log.error("parse message json fail : {}", e.getMessage());
            }
        } else {
            log.warn("Parse msg error. {}", message);
        }
        return null;
    }

    //解析消息类型
    private Type getMessageType() {

        // 获取当前运行类父类及其泛型类型，即为参数化类型（Type），由所有类型公用的高级接口Type接收
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            // 强转，ParameterizedType参数化类型，即泛型 (T)
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            // 获取参数化类型中，实际类型的定义
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
           //断言机制，相当于三元表达式
            Assert.isTrue(actualTypeArguments.length == 1, "Number of type arguments must be 1");
            // 接下来便可转换 Class c= (Class) actualTypeArguments[0];
            return actualTypeArguments[0];
        } else
            // 如果没有定义泛型，解析为Object
            return Object.class;

    }

    protected Map<String, Object> parseExtParam(MessageExt message) {
        Map<String, Object> extMap = new HashMap<>();

        // parse message property
        extMap.put(MessageExtConst.PROPERTY_TOPIC, message.getTopic());
        extMap.putAll(message.getProperties());

        // parse messageExt property ,消息拓展类
        extMap.put(MessageExtConst.PROPERTY_EXT_BORN_HOST, message.getBornHost());
        extMap.put(MessageExtConst.PROPERTY_EXT_BORN_TIMESTAMP, message.getBornTimestamp());
        extMap.put(MessageExtConst.PROPERTY_EXT_COMMIT_LOG_OFFSET, message.getCommitLogOffset());
        extMap.put(MessageExtConst.PROPERTY_EXT_MSG_ID, message.getMsgId());
        extMap.put(MessageExtConst.PROPERTY_EXT_PREPARED_TRANSACTION_OFFSET, message.getPreparedTransactionOffset());
        extMap.put(MessageExtConst.PROPERTY_EXT_QUEUE_ID, message.getQueueId());
        extMap.put(MessageExtConst.PROPERTY_EXT_QUEUE_OFFSET, message.getQueueOffset());
        extMap.put(MessageExtConst.PROPERTY_EXT_RECONSUME_TIMES, message.getReconsumeTimes());
        extMap.put(MessageExtConst.PROPERTY_EXT_STORE_HOST, message.getStoreHost());
        extMap.put(MessageExtConst.PROPERTY_EXT_STORE_SIZE, message.getStoreSize());
        extMap.put(MessageExtConst.PROPERTY_EXT_STORE_TIMESTAMP, message.getStoreTimestamp());
        extMap.put(MessageExtConst.PROPERTY_EXT_SYS_FLAG, message.getSysFlag());
        extMap.put(MessageExtConst.PROPERTY_EXT_BODY_CRC, message.getBodyCRC());

        return extMap;
    }
}
