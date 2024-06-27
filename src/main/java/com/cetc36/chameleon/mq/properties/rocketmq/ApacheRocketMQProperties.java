package com.cetc36.chameleon.mq.properties.rocketmq;

import com.cetc36.chameleon.mq.properties.BaseProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.poller.ApacheMQPollProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.publisher.ApacheMQPubProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.subscriber.ApacheMQSubProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Ocean配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cetc36.mq.ocean")
public class ApacheRocketMQProperties extends BaseProperties {
    /**
     * mq 注册中心,服务地址
     */
    private String nameServerAddr;

    /**
     * 本机IP
     * 客户端本机IP地址，某些机器会发生无法识别客户端IP地址情况，需要应用在代码中强制指定
     */
    private String clientIp;

    /**
     * 通信层异步回调线程数 默认4个
     */
    private Integer clientCallbackExecutorThreads;

    /**
     * 轮询Name Server间隔时间，单位毫秒     30000
     */
    private Integer pollNameServerInteval;

    /**
     * 向Broker发送心跳间隔时间，单位毫秒  30000
     */
    private Integer heartbeatBrokerInterval;

    /**
     * 持久化Consumer消费进度间隔时间，单位毫秒  5000
     */
    private Integer persistConsumerOffsetInterval;

    /**
     * 消息生产者
     */
    private List<ApacheMQPubProperties> publishers;

    /**
     * 消息订阅者
     */
    private List<ApacheMQSubProperties> subscribers;

    /**
     * 消息拉取者
     */
    private List<ApacheMQPollProperties> pollers;

}
