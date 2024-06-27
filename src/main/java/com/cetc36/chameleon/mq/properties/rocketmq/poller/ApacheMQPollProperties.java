package com.cetc36.chameleon.mq.properties.rocketmq.poller;

import com.cetc36.chameleon.mq.properties.BaseSubProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息消费者配置
 */
@Setter
@Getter
public class ApacheMQPollProperties extends BaseSubProperties {

    /**
     * 消息消费者在spring中的beanName
     */
    private String beanName;

    /**
     * 生产者和消费者归属的组id, 一个微服务一个groupId 不能为空！！！
     */
    private String groupId;

    /**
     * 主题名
     */
    private String topicName;

    /**
     * 过滤表达式
     */
    private String tagExpression;

    /**
     * 拉取数量
     */
    private Integer pullBatchSize;

}
