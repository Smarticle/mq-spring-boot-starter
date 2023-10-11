package com.cetc36.chameleon.mq.service.impl.ocean;

import com.cetc36.chameleon.mq.api.TopicPublisher;
import com.cetc36.chameleon.mq.api.TopicSendCallback;
import com.cetc36.chameleon.mq.exception.TopicMQException;
import com.cetc36.chameleon.mq.model.TopicMessage;
import com.cetc36.chameleon.mq.model.TopicMessageSendResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

/**
 * ApacheRocketMq推送消息到MQ服务端
 * 发布（pub）模式
 *
 * @author liuyang
 */
@Getter
@Setter
@Slf4j
@SuppressWarnings("unused")
public class SimpleOceanPublisher implements TopicPublisher {

    /**
     * apache rocketmq producer
     */
    DefaultMQProducer producer;

    String beanName;

    boolean isStarted;

    public SimpleOceanPublisher(DefaultMQProducer producer, String beanName) {
        this.producer = producer;
        this.beanName = beanName;
    }

    public SimpleOceanPublisher() {
    }

    @Override
    public TopicMessageSendResult publish(TopicMessage topicMessage) {
        Message message = converRocketMessage(topicMessage);
        SendResult sendResult;
        try {
            sendResult = producer.send(message);
        } catch (Exception e) {
            TopicMQException topicMqException = new TopicMQException("ApacheRocketMq keys =" + message.getKeys()
                    + " 发送异常 ", message.getTopic(), e);
            topicMqException.setMessageId(topicMessage.getMessageId());
            topicMqException.setTag(topicMessage.getTags());
            topicMqException.setBizId(topicMessage.getBizId());
            throw topicMqException;
        }
        if (!sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
            TopicMQException topicMqException = new TopicMQException("ApacheRocketMq keys =" + message.getKeys()
                    + "发送异常 " + sendResult.getSendStatus().toString(), message.getTopic());
            topicMqException.setMessageId(sendResult.getMsgId());
            topicMqException.setTag(topicMessage.getTags());
            topicMqException.setBizId(topicMessage.getBizId());
            throw topicMqException;
        }
        TopicMessageSendResult topicMessageSendResult = new TopicMessageSendResult();
        topicMessageSendResult.setMessageId(sendResult.getMsgId());
        topicMessageSendResult.setBizId(message.getKeys());
        topicMessageSendResult.setTags(message.getTags());
        if (sendResult.getMessageQueue() != null) {
            topicMessageSendResult.setTopicName(sendResult.getMessageQueue().getTopic());
        }
        return topicMessageSendResult;
    }

    /**
     * RocketMQ消息适配
     */
    static Message converRocketMessage(TopicMessage topicMessage) {
        Message message = new Message();
        if (topicMessage.getUserProperties() != null) {
            topicMessage.getUserProperties().forEach((k, v) -> message.putUserProperty(k.toString(), v.toString()));
        }
        message.setKeys(topicMessage.getBizId());
        message.setBody(topicMessage.getMessageBody());
        message.setTags(topicMessage.getTags());
        message.setTopic(topicMessage.getTopicName());
        return message;
    }

    @Override
    public void publishAsync(TopicMessage topicMessage, TopicSendCallback topicSendCallback) {
        Message message = converRocketMessage(topicMessage);
        try {
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    if (!sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                        throw new TopicMQException("Ocean businessKey =" + message.getKeys() + " 异步发送 onSuccess方法异常 " + sendResult.getSendStatus().toString(), message.getTopic());
                    }
                    TopicMessageSendResult topicMessageSendResult = new TopicMessageSendResult();
                    if (sendResult.getMessageQueue() != null) {
                        topicMessageSendResult.setTopicName(sendResult.getMessageQueue().getTopic());
                    }
                    topicMessageSendResult.setMessageId(sendResult.getMsgId());
                    topicMessageSendResult.setBizId(message.getKeys());
                    topicMessageSendResult.setTags(message.getTags());
                    topicSendCallback.onSuccess(topicMessageSendResult);
                }

                @Override
                public void onException(Throwable e) {
                    TopicMQException topicMqException = new TopicMQException(e);
                    topicMqException.setTopicName(message.getTopic());
                    topicMqException.setMessageId(message.getBuyerId());
                    topicMqException.setBizId(message.getKeys());
                    topicMqException.setTag(message.getTags());
                    topicSendCallback.onFail(topicMqException);
                }
            });
        } catch (Exception e) {
            throw new TopicMQException("Ocean businessKey =" + message.getKeys() + " 异步发送异常 ", message.getTopic(), e);
        }
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
        log.info("【MQ】SimpleOceanPublisher[" + beanName + "] start...");
        try {
            producer.start();
            isStarted = true;
        } catch (MQClientException e) {
            log.error("【MQ】SimpleOceanPublisher[" + beanName + "] start error", e);
        }
    }

    @Override
    public void close() {
        log.info("【MQ】SimpleOceanPublisher[" + beanName + "] close...");
        producer.shutdown();
    }

}
