package com.cetc36.chameleon.mq.config;

import com.cetc36.chameleon.mq.api.TopicPoller;
import com.cetc36.chameleon.mq.api.TopicPollerFactory;
import com.cetc36.chameleon.mq.properties.rocketmq.ApacheRocketMQProperties;
import com.cetc36.chameleon.mq.service.impl.ocean.ApacheRocketMQPoller;
import com.cetc36.chameleon.mq.util.BeanArgBuilder;
import com.cetc36.chameleon.mq.util.BeanRegisterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ApacheRocketMQTopicPollerFactory implements TopicPollerFactory {

    private final ApacheRocketMQProperties apacheRocketMQProperties;

    private ApplicationContext applicationContext;

    /**
     * 拉取者Map
     */
    public static Map<String, TopicPoller> topicPollerMap = new ConcurrentHashMap<>();

    public ApacheRocketMQTopicPollerFactory(ApacheRocketMQProperties apacheRocketMQProperties, ApplicationContext applicationContext) {
        this.apacheRocketMQProperties = apacheRocketMQProperties;
        this.applicationContext = applicationContext;
    }


    public static String generateTopicPollerKey(String groupId, String topicName, String tagExpression) {
        return groupId + "_liu_" + topicName + "_yang_" + tagExpression;
    }

    /**
     * 创建TopicPoller
     */
    public TopicPoller create(String groupId, String topicName, String tagExpression, int pullBatchSize) {
        String key = generateTopicPollerKey(groupId, topicName, tagExpression);
        if (topicPollerMap.containsKey(key)) {
            // TODO 重新设置其他参数
            return topicPollerMap.get(key);
        }
        DefaultLitePullConsumer poller = new DefaultLitePullConsumer(groupId);
        // 公共配置
        setCommonConfig(poller);
        // 订阅消息
        try {
            poller.subscribe(topicName);
        } catch (MQClientException e) {
            log.error("【MQ】ApacheRocketMQPoller[" + topicName + tagExpression + "] subscribe error", e);
            return null;
        }
        // 设置拉取数量
        poller.setPullBatchSize(pullBatchSize);
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanArgBuilder beanArgBuilder = new BeanArgBuilder();
        beanArgBuilder.setConstructorArgs(new Object[]{poller, topicName});
        beanArgBuilder.setDestroyMethodName("close");
        String beanName = "ApacheRocketMQ" + key;
        BeanRegisterUtil.registerBean(defaultListableBeanFactory, beanName, ApacheRocketMQPoller.class, beanArgBuilder);
        ApacheRocketMQPoller pollerBean = applicationContext.getBean(beanName, ApacheRocketMQPoller.class);
        // 启动
        pollerBean.start();
        topicPollerMap.put(key, pollerBean);
        return pollerBean;
    }

    /**
     * 公共配置
     */
    private void setCommonConfig(ClientConfig config) {
        if (StringUtils.isNotBlank(apacheRocketMQProperties.getClientIp())) {
            config.setClientIP(apacheRocketMQProperties.getClientIp());
        }
        if (apacheRocketMQProperties.getClientCallbackExecutorThreads() != null) {
            config.setClientCallbackExecutorThreads(apacheRocketMQProperties.getClientCallbackExecutorThreads());
        }
        if (apacheRocketMQProperties.getHeartbeatBrokerInterval() != null) {
            config.setHeartbeatBrokerInterval(apacheRocketMQProperties.getHeartbeatBrokerInterval());
        }
        if (apacheRocketMQProperties.getPollNameServerInteval() != null) {
            config.setPollNameServerInterval(apacheRocketMQProperties.getPollNameServerInteval());
        }
        if (apacheRocketMQProperties.getPersistConsumerOffsetInterval() != null) {
            config.setPersistConsumerOffsetInterval(apacheRocketMQProperties.getPersistConsumerOffsetInterval());
        }
        // TODO 校验
        config.setNamesrvAddr(apacheRocketMQProperties.getNameServerAddr());
    }

}
