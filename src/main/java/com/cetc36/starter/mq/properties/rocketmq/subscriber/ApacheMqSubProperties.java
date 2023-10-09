package com.cetc36.starter.mq.properties.rocketmq.subscriber;

import com.cetc36.starter.mq.properties.BaseSubProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息消费者配置
 */
@Setter
@Getter
public class ApacheMqSubProperties extends BaseSubProperties {

    /**
     * 消息消费者在spring中的beanName
     */
    private String beanName;

    /**
     * 生产者和消费者归属的组id, 一个微服务一个groupId 不能为空！！！
     */
    private String groupId;

    /**
     * 消费线程池最小线程数
     */
    private Integer consumeThreadMin;

    /**
     * 消费线程池最大线程数
     */
    private Integer consumeThreadMax;

    /**
     * 单队列并行消费允许的最大跨度
     */
    private Integer consumeConcurrentlyMaxSpan;

    /**
     * 拉消息本地队列缓存消息最大数
     */
    private Integer pullThresholdForQueue;

}
