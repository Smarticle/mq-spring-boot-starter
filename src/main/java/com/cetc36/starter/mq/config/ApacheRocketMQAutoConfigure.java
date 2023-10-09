package com.cetc36.starter.mq.config;

import com.cetc36.starter.mq.properties.rocketmq.ApacheRocketMQProperties;
import com.cetc36.starter.mq.properties.rocketmq.publisher.ApacheMQPubProperties;
import com.cetc36.starter.mq.properties.rocketmq.subscriber.ApacheMqSubProperties;
import com.cetc36.starter.mq.service.RetryConsumeFailHandler;
import com.cetc36.starter.mq.service.TopicListener;
import com.cetc36.starter.mq.service.impl.DefaultRetryConsumeFailHandler;
import com.cetc36.starter.mq.service.impl.DefaultTopicListenerImpl;
import com.cetc36.starter.mq.service.impl.rocketmq.ApacheSimpleRocketMQPublisher;
import com.cetc36.starter.mq.service.impl.rocketmq.ApacheSimpleRocketMQSubscriber;
import com.cetc36.starter.mq.util.BeanArgBuilder;
import com.cetc36.starter.mq.util.BeanRegistrarUtil;
import com.cetc36.starter.mq.util.MQUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RocketMQ 配置类
 *
 * @author liuyang
 */
@Slf4j
@Configuration
@ConditionalOnClass({SendMessageContext.class})
@ConditionalOnProperty(prefix = "cetc36.mq.rocketmq", value = "enable", havingValue = "true")
@EnableConfigurationProperties({ApacheRocketMQProperties.class})
public class ApacheRocketMQAutoConfigure implements InitializingBean {

    @Resource
    private ApacheRocketMQProperties apacheRocketMQProperties;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 消息消费者
     */
    @Autowired
    private Map<String, TopicListener> listenerMap = new ConcurrentHashMap<>(4);

    @Bean
    @ConditionalOnMissingBean
    public RetryConsumeFailHandler defaultRetryConsumeFailHandler() {
        return new DefaultRetryConsumeFailHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicListener defaultTopicListener() {
        return new DefaultTopicListenerImpl();
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        // 发布者
        topicPubService();
        // 订阅者
        topicSubService();
    }

    /**
     * 发布服务注入容器
     */
    private void topicPubService() {
        List<ApacheMQPubProperties> properties = apacheRocketMQProperties.getPublishers();
        if (properties == null || properties.isEmpty()) {
            log.info("没有配置消息发布者的属性, 不初始化消息发布者对象");
            return;
        }

        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        for (ApacheMQPubProperties pubItem : properties) {
            DefaultMQProducer producer = new DefaultMQProducer(pubItem.getGroupId());
            // 公共配置
            setCommonConfig(producer);
            // 生产者个性化配置
            if (pubItem.getSendMsgTimeout() != null) {
                producer.setSendMsgTimeout(pubItem.getSendMsgTimeout());
            }
            if (pubItem.getMaxMessageSize() != null) {
                producer.setMaxMessageSize(pubItem.getMaxMessageSize());
            }
            if (pubItem.getCompressMsgBodyOverHowmuch() != null) {
                producer.setCompressMsgBodyOverHowmuch(pubItem.getCompressMsgBodyOverHowmuch());
            }
            if (pubItem.getRetryTimesWhenSendFailed() != null) {
                producer.setRetryTimesWhenSendFailed(pubItem.getRetryTimesWhenSendFailed());
            }
            if (pubItem.getRetryAnotherBrokerWhenNotStoreOK() != null) {
                producer.setRetryAnotherBrokerWhenNotStoreOK(pubItem.getRetryAnotherBrokerWhenNotStoreOK());
            }
            // 生产者注入容器
            BeanArgBuilder beanArgBuilder = new BeanArgBuilder();
            beanArgBuilder.setConstructorArgs(new Object[]{producer, pubItem.getBeanName()});
            beanArgBuilder.setInitMethodName("start");
            beanArgBuilder.setDestroyMethodName("close");
            BeanRegistrarUtil.registerBean(defaultListableBeanFactory, pubItem.getBeanName(), ApacheSimpleRocketMQPublisher.class, beanArgBuilder);
        }
    }

    /**
     * 订阅服务注入容器
     */
    private void topicSubService() throws IOException {
        if (listenerMap == null || listenerMap.isEmpty()) {
            log.info("没有消息监听者service对象, 不初始化消息消费者对象");
            return;
        }

        List<ApacheMqSubProperties> properties = apacheRocketMQProperties.getSubscribers();
        if (properties == null || properties.isEmpty()) {
            log.info("没有配置消息消息者的属性, 不初始化消息消费者对象");
            return;
        }
        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        RetryConsumeFailHandler retryConsumeFailHandler = applicationContext.getBean(RetryConsumeFailHandler.class);

        Map<String, List<TopicListener>> listenerMapBySubBeanName = MQUtil.topicListenerGroupBySubBean(listenerMap);

        for (ApacheMqSubProperties subItem : properties) {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(subItem.getGroupId());
            // 公共配置
            setCommonConfig(consumer);
            // Wrong time format 2017_0422_221800
            consumer.setConsumeTimestamp("20180422221800");
            BeanArgBuilder beanArgBuilder = new BeanArgBuilder();
            beanArgBuilder.setConstructorArgs(new Object[]{consumer, subItem});
            // start 方法在MQUtil.setListenerAndStartSub
            beanArgBuilder.setDestroyMethodName("close");
            BeanRegistrarUtil.registerBean(defaultListableBeanFactory, subItem.getBeanName(), ApacheSimpleRocketMQSubscriber.class, beanArgBuilder);
            ApacheSimpleRocketMQSubscriber subscriber = applicationContext.getBean(subItem.getBeanName(), ApacheSimpleRocketMQSubscriber.class);
            MQUtil.setListenerAndStartSub(subItem.getBeanName(), subscriber, retryConsumeFailHandler, listenerMapBySubBeanName);
        }
    }

    /**
     * 公共配置
     */
    private void setCommonConfig(ClientConfig config) {
        if (StringUtils.hasText(apacheRocketMQProperties.getClientIp())) {
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
        config.setNamesrvAddr(apacheRocketMQProperties.getNameServerAddr());
    }
}
