package com.cetc36.chameleon.mq.config;

import com.cetc36.chameleon.mq.api.ConsumeFailHandler;
import com.cetc36.chameleon.mq.api.TopicListener;
import com.cetc36.chameleon.mq.api.TopicPoller;
import com.cetc36.chameleon.mq.api.TopicSubscriber;
import com.cetc36.chameleon.mq.properties.rocketmq.ApacheRocketMQProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.poller.ApacheRocketMQPollProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.publisher.ApacherRocketMQPubProperties;
import com.cetc36.chameleon.mq.properties.rocketmq.subscriber.ApacheRocketMQSubProperties;
import com.cetc36.chameleon.mq.service.impl.DefaultConsumeFailHandler;
import com.cetc36.chameleon.mq.service.impl.DefaultTopicListenerImpl;
import com.cetc36.chameleon.mq.service.impl.ocean.ApacheRocketMQPoller;
import com.cetc36.chameleon.mq.service.impl.ocean.ApacheRocketMQPublisher;
import com.cetc36.chameleon.mq.service.impl.ocean.ApacheRocketMQSubscriber;
import com.cetc36.chameleon.mq.util.BeanArgBuilder;
import com.cetc36.chameleon.mq.util.BeanRegisterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cetc36.chameleon.mq.config.ApacheRocketMQTopicPollerFactory.generateTopicPollerKey;
import static java.util.stream.Collectors.groupingBy;

/**
 * RocketMQ 配置类
 *
 * @author liuyang
 */
@Slf4j
@Configuration
@ConditionalOnClass({SendMessageContext.class})
@ConditionalOnProperty(prefix = "cetc36.mq.ocean", value = "enable", havingValue = "true")
@EnableConfigurationProperties({ApacheRocketMQProperties.class})
public class ApacheRocketMQAutoConfigure implements InitializingBean {

    @Autowired
    private ApacheRocketMQProperties apacheRocketMQProperties;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Spring可自动注入TopicListener，Key为beanName
     */
    @Autowired
    private Map<String, TopicListener> listenerMap = new ConcurrentHashMap<>(4);

    /**
     * TopicPollerMap，Key为TopicName_TagExpression
     */
    private static Map<String, TopicPoller> topicPollerMap = new ConcurrentHashMap<>(4);

    @Bean
    @ConditionalOnMissingBean
    public ConsumeFailHandler defaultRetryConsumeFailHandler() {
        return new DefaultConsumeFailHandler();
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
        // 拉取者
         topicPollService();
    }

    /**
     * 发布服务注入容器
     */
    private void topicPubService() {
        List<ApacherRocketMQPubProperties> properties = apacheRocketMQProperties.getPublishers();
        if (properties == null || properties.isEmpty()) {
            log.info("没有配置消息发布者的属性, 不初始化消息发布者对象");
            return;
        }

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        for (ApacherRocketMQPubProperties pubItem : properties) {
            // TODO 校验配置
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
            BeanRegisterUtil.registerBean(defaultListableBeanFactory, pubItem.getBeanName(), ApacheRocketMQPublisher.class, beanArgBuilder);
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
        List<ApacheRocketMQSubProperties> properties = apacheRocketMQProperties.getSubscribers();
        if (properties == null || properties.isEmpty()) {
            log.info("没有配置消息消息者的属性, 不初始化消息消费者对象");
            return;
        }
        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        ConsumeFailHandler consumeFailHandler = applicationContext.getBean(ConsumeFailHandler.class);
        Map<String, List<TopicListener>> listenerMapBySubBeanName = topicListenerGroupBySubBean(listenerMap);

        for (ApacheRocketMQSubProperties subItem : properties) {
            // TODO 校验配置
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(subItem.getGroupId());
            // 公共配置
            setCommonConfig(consumer);
            // Wrong time format 2017_0422_221800
            //consumer.setConsumeTimestamp("20180422221800");
            // Listener设置
            BeanArgBuilder beanArgBuilder = new BeanArgBuilder();
            beanArgBuilder.setConstructorArgs(new Object[]{consumer, subItem});
            // start方法setListener后手动调用
            beanArgBuilder.setDestroyMethodName("close");
            BeanRegisterUtil.registerBean(defaultListableBeanFactory, subItem.getBeanName(), ApacheRocketMQSubscriber.class, beanArgBuilder);
            ApacheRocketMQSubscriber subscriber = applicationContext.getBean(subItem.getBeanName(), ApacheRocketMQSubscriber.class);
            setListener(subItem.getBeanName(), subscriber, consumeFailHandler, listenerMapBySubBeanName);
            // 启动
            subscriber.start();
        }
    }

    /**
     * 订阅服务注入容器
     */
    private void topicPollService() {
        List<ApacheRocketMQPollProperties> properties = apacheRocketMQProperties.getPollers();
        if (properties == null || properties.isEmpty()) {
            log.info("没有配置消息拉取者的属性, 不初始化消息拉取者对象");
            return;
        }
        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        for (ApacheRocketMQPollProperties pollItem : properties) {
            // 校验配置
            if (StringUtils.isBlank(pollItem.getTopicName()) || StringUtils.isBlank(pollItem.getTagExpression()) || StringUtils.isBlank(pollItem.getGroupId())) {
                log.error("【MQ】ApacheRocketMQPoller pollItem必填字段为空");
                continue;
            }
            String key = generateTopicPollerKey(pollItem.getGroupId(), pollItem.getTopicName(), pollItem.getTagExpression());
            if (topicPollerMap.containsKey(key)) {
                log.error("【MQ】ApacheRocketMQPoller topicPollerMap" + key + "已存在");
                continue;
            }
            DefaultLitePullConsumer poller = new DefaultLitePullConsumer(pollItem.getGroupId());
            // 公共配置
            setCommonConfig(poller);
            // 订阅消息
            try {
                poller.subscribe(pollItem.getTopicName(), pollItem.getTagExpression());
            } catch (MQClientException e) {
                log.error("【MQ】ApacheRocketMQPoller[" + pollItem.getBeanName() + "] subscribe error", e);
                return;
            }
            // 设置拉取数量
            if (pollItem.getPullBatchSize() != null) {
                poller.setPullBatchSize(pollItem.getPullBatchSize());
            }
            BeanArgBuilder beanArgBuilder = new BeanArgBuilder();
            beanArgBuilder.setConstructorArgs(new Object[]{poller, pollItem.getBeanName()});
            beanArgBuilder.setDestroyMethodName("close");
            BeanRegisterUtil.registerBean(defaultListableBeanFactory, pollItem.getBeanName(), ApacheRocketMQPoller.class, beanArgBuilder);
            ApacheRocketMQPoller pollerBean = applicationContext.getBean(pollItem.getBeanName(), ApacheRocketMQPoller.class);
            // 启动
            pollerBean.start();
            topicPollerMap.put(key, pollerBean);
        }
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

    /**
     * 设置消费服务TopicListener，并且启动消费服务
     *
     * @param subscriberBeanName       ignore
     * @param subscriber               ignore
     * @param listenerMapBySubBeanName ignore
     * @param consumeFailHandler       ignore
     */
    private void setListener(String subscriberBeanName, TopicSubscriber subscriber, ConsumeFailHandler consumeFailHandler, Map<String, List<TopicListener>> listenerMapBySubBeanName) throws IOException {

        List<TopicListener> listeners = listenerMapBySubBeanName.get(subscriberBeanName);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (TopicListener listener : listeners) {
            subscriber.subscribe(listener.getTopicName(), listener.getTagExpression(), listener);
        }
        subscriber.setRetryConsumeFailHandler(consumeFailHandler);
    }

    /**
     * TopicListener 按自定义BeanName分组
     * key：SubscriberBeanName Value: 相应TopicListener
     *
     * @param listenerMap TopicListener
     * @return Map
     */
    private Map<String, List<TopicListener>> topicListenerGroupBySubBean(Map<String, TopicListener> listenerMap) {
        List<TopicListener> topicListenerList = new ArrayList<>(4);
        listenerMap.forEach((k, v) -> topicListenerList.add(v));
        //groupBy SubscriberBeanName
        return topicListenerList.stream().collect(groupingBy(TopicListener::getSubscriberBeanName));
    }
}
