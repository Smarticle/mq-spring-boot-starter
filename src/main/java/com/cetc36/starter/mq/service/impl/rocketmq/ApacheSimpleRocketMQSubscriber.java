package com.cetc36.starter.mq.service.impl.rocketmq;

import com.cetc36.starter.mq.model.MessageStatus;
import com.cetc36.starter.mq.model.TopicMessage;
import com.cetc36.starter.mq.properties.rocketmq.subscriber.ApacheMQSubProperties;
import com.cetc36.starter.mq.api.ConsumeFailHandler;
import com.cetc36.starter.mq.api.TopicListener;
import com.cetc36.starter.mq.api.TopicSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Map;
import java.util.Properties;

/**
 * ApacheRocketMq 订阅消息
 */
@Slf4j
@SuppressWarnings("unused")
public class ApacheSimpleRocketMQSubscriber implements TopicSubscriber {

    /**
     * rocketMq消费服务
     */
    DefaultMQPushConsumer consumer;

    String beanName;

    boolean isStarted;

    ApacheMQSubProperties apacheMQSubProperties;

    ConsumeFailHandler consumeFailHandler;

    public ApacheSimpleRocketMQSubscriber(DefaultMQPushConsumer consumer, ApacheMQSubProperties apacheMQSubProperties) {
        this.consumer = consumer;
        this.beanName = apacheMQSubProperties.getBeanName();
        this.apacheMQSubProperties = apacheMQSubProperties;
    }

    public ApacheSimpleRocketMQSubscriber() {
    }

    @Override
    public void subscribe(String topic, String tagExpression, TopicListener listener) {
        try {
            consumer.subscribe(topic, tagExpression);
        } catch (MQClientException e) {
            log.error("【MQ】ApacheSimpleRocketMqSubscriber[" + beanName + "] start error", e);
            return;
        }
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
            // 默认一次消费一条，批量消费支持可修改此处 messages.size() >= 1  DefaultMQPushConsumer.consumeMessageBatchMaxSize=1
            MessageExt messageExt = messages.get(0);
            // 转换成TopicMessage
            TopicMessage topicMessage = new TopicMessage();
            Map<String, String> RocketMQUserPropertiesMap = messageExt.getProperties();
            if (RocketMQUserPropertiesMap != null && !RocketMQUserPropertiesMap.isEmpty()) {
                Properties userProperties = new Properties();
                userProperties.putAll(RocketMQUserPropertiesMap);
                topicMessage.setUserProperties(userProperties);
            }
            topicMessage.setMessageId(messageExt.getMsgId());
            topicMessage.setBizId(messageExt.getKeys());
            topicMessage.setMessageBody(messageExt.getBody());
            topicMessage.setTags(messageExt.getTags());
            topicMessage.setTopicName(messageExt.getTopic());
            topicMessage.setCurrentRetryConsumeCount(messageExt.getReconsumeTimes());
            // 回调此listener
            MessageStatus messageStatus = listener.subscribe(topicMessage);
            if (messageStatus.equals(MessageStatus.ConsumeSuccess)) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } else {
                // 消费失败
                return failureFrequency(topicMessage);
            }
        });
    }

    /**
     * 消费失败处理
     */
    protected ConsumeConcurrentlyStatus failureFrequency(TopicMessage topicMessage) {
        // String messageUniqueId = topicMessage.getTopicName() + "_" + topicMessage.getTags() + "_" + topicMessage.getMessageId();
        int retryCnt = topicMessage.getCurrentRetryConsumeCount();
        if (retryCnt >= apacheMQSubProperties.getMaxRetryCount()) {
            log.info("消息超过最大重新投递次数{} ，直接消费完成！ topicName={}, messageId={}, bizId={}, routingKey={}, groupId={}",
                    apacheMQSubProperties.getMaxRetryCount(), topicMessage.getTopicName(),
                    topicMessage.getMessageId(), topicMessage.getBizId(), topicMessage.getTags(), apacheMQSubProperties.getGroupId());
            consumeFailHandler.handle(topicMessage);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } else {
            log.info("消息重新投递.... topicName={}, messageId={}, bizId={}, routingKey={}, groupId={}",
                    topicMessage.getTopicName(), topicMessage.getMessageId(), topicMessage.getBizId(),
                    topicMessage.getTags(), apacheMQSubProperties.getGroupId());
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    @Override
    public void unsubscribe(String topicName) {
        consumer.unsubscribe(topicName);
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public boolean isClosed() {
        return !isStarted;
    }

    @Override
    public void start() {
        log.info("【MQ】ApacheSimpleRocketMqSubscriber[" + beanName + "] start...");
        try {
            consumer.start();
            isStarted = true;
        } catch (MQClientException e) {
            log.error("【MQ】ApacheSimpleRocketMqSubscriber[" + beanName + "] start error", e);
        }
    }

    @Override
    public void close() {
        log.info("【MQ】ApacheSimpleRocketMqSubscriber[" + beanName + "] close...");
        consumer.shutdown();
    }

    @Override
    public void setRetryConsumeFailHandler(ConsumeFailHandler consumeFailHandler) {
        this.consumeFailHandler = consumeFailHandler;
    }

    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DefaultMQPushConsumer consumer) {
        this.consumer = consumer;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
