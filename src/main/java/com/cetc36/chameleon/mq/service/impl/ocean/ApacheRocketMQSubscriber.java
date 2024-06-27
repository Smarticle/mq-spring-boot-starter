package com.cetc36.chameleon.mq.service.impl.ocean;

import com.cetc36.chameleon.mq.api.ConsumeFailHandler;
import com.cetc36.chameleon.mq.api.TopicListener;
import com.cetc36.chameleon.mq.api.TopicSubscriber;
import com.cetc36.chameleon.mq.model.MessageStatus;
import com.cetc36.chameleon.mq.model.TopicMessage;
import com.cetc36.chameleon.mq.properties.rocketmq.subscriber.ApacheRocketMQSubProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import static com.cetc36.chameleon.mq.util.ApacheRocketMQUtil.convertToCETCMessage;

/**
 * ApacheRocketMq 订阅消息
 */
@Slf4j
@SuppressWarnings("unused")
public class ApacheRocketMQSubscriber implements TopicSubscriber {

    /**
     * rocketMq消费服务
     */
    DefaultMQPushConsumer consumer;

    String beanName;

    boolean isStarted;

    ApacheRocketMQSubProperties apacheRocketMQSubProperties;

    ConsumeFailHandler consumeFailHandler;

    public ApacheRocketMQSubscriber(DefaultMQPushConsumer consumer, ApacheRocketMQSubProperties apacheRocketMQSubProperties) {
        this.consumer = consumer;
        this.beanName = apacheRocketMQSubProperties.getBeanName();
        this.apacheRocketMQSubProperties = apacheRocketMQSubProperties;
    }

    public ApacheRocketMQSubscriber() {
    }

    @Override
    public void subscribe(String topic, String tagExpression, TopicListener listener) {
        try {
            consumer.subscribe(topic, tagExpression);
        } catch (MQClientException e) {
            log.error("【MQ】ApacheRocketMQSubscriber[" + beanName + "] start error", e);
            return;
        }
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
            // 默认一次消费一条，批量消费支持可修改此处 messages.size() >= 1  DefaultMQPushConsumer.consumeMessageBatchMaxSize=1
            MessageExt messageExt = messages.get(0);
            // 转换成TopicMessage
            TopicMessage topicMessage = convertToCETCMessage(messageExt);
            // 回调此listener
            MessageStatus messageStatus = listener.consume(topicMessage);
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
        if (retryCnt >= apacheRocketMQSubProperties.getMaxRetryCount()) {
            log.info("消息超过最大重新投递次数{} ，直接消费完成！ topicName={}, messageId={}, bizId={}, routingKey={}, groupId={}",
                    apacheRocketMQSubProperties.getMaxRetryCount(), topicMessage.getTopicName(),
                    topicMessage.getMessageId(), topicMessage.getBizId(), topicMessage.getTags(), apacheRocketMQSubProperties.getGroupId());
            consumeFailHandler.handle(topicMessage);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } else {
            log.info("消息重新投递.... topicName={}, messageId={}, bizId={}, routingKey={}, groupId={}",
                    topicMessage.getTopicName(), topicMessage.getMessageId(), topicMessage.getBizId(),
                    topicMessage.getTags(), apacheRocketMQSubProperties.getGroupId());
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
        log.info("【MQ】ApacheRocketMQSubscriber[" + beanName + "] start...");
        try {
            consumer.start();
            isStarted = true;
        } catch (MQClientException e) {
            log.error("【MQ】ApacheRocketMQSubscriber[" + beanName + "] start error", e);
        }
    }

    @Override
    public void close() {
        log.info("【MQ】ApacheRocketMQSubscriber[" + beanName + "] close...");
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
