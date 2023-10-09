package com.cetc36.starter.mq.service.impl;

import com.cetc36.starter.mq.model.MessageStatus;
import com.cetc36.starter.mq.model.TopicMessage;
import com.cetc36.starter.mq.TopicListener;
import lombok.extern.slf4j.Slf4j;

/**
 * TopicListener 的默认实现，主要用于容错
 *
 * @author liuyang
 */
@Slf4j
public class DefaultTopicListenerImpl implements TopicListener {

    public static String DEFAULT_SUBSCRIBER_BEANNAME = "default_SubscriberBeanName";
    public static String DEFAULT_TOPIC_NAME = "default_TopicName";
    public static String DEFAULT_TAG = "default_Tag";

    @Override
    public String getSubscriberBeanName() {
        return DEFAULT_SUBSCRIBER_BEANNAME;
    }

    @Override
    public String getTopicName() {
        return DEFAULT_TOPIC_NAME;
    }

    @Override
    public String getTagExpression() {
        return DEFAULT_TAG;
    }

    @Override
    public MessageStatus subscribe(TopicMessage topicMessage) {
        if (topicMessage != null) {
            log.debug("消息 id={}, key={} 被 DefaultTopicListenerImpl 消费了", topicMessage.getMessageId(), topicMessage.getBizId());
            return MessageStatus.ConsumeSuccess;
        } else {
            return MessageStatus.ConsumeFail;
        }
    }
}
