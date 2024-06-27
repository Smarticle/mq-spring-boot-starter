package com.cetc36.chameleon.mq.util;

import com.cetc36.chameleon.mq.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.*;


/**
 * 工具类
 */
@Slf4j
public class ApacheRocketMQUtil {

    /**
     * CETC消息转换为RocketMQ消息
     */
    public static Message converToRocketMessage(TopicMessage topicMessage) {
        if (topicMessage == null) {
            return null;
        }
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

    /**
     * RocketMQ消息转换为CETC消息列表
     */
    public static List<TopicMessage> convertToCETCMessageList(List<MessageExt> rocketMQMessages) {
        if (rocketMQMessages == null || rocketMQMessages.isEmpty()) {
            return Collections.emptyList();
        }
        List<TopicMessage> cetcMessageList = new ArrayList<>();
        for (MessageExt messageExt : rocketMQMessages) {
            cetcMessageList.add(convertToCETCMessage(messageExt));
        }
        return cetcMessageList;
    }

    /**
     * RocketMQ消息转换为CETC消息
     */
    public static TopicMessage convertToCETCMessage(MessageExt messageExt) {
        if (messageExt == null) {
            return null;
        }
        TopicMessage cetcMessage = new TopicMessage();
        Map<String, String> RocketMQUserPropertiesMap = messageExt.getProperties();
        if (RocketMQUserPropertiesMap != null && !RocketMQUserPropertiesMap.isEmpty()) {
            Properties userProperties = new Properties();
            userProperties.putAll(RocketMQUserPropertiesMap);
            cetcMessage.setUserProperties(userProperties);
        }
        cetcMessage.setMessageId(messageExt.getMsgId());
        cetcMessage.setBizId(messageExt.getKeys());
        cetcMessage.setMessageBody(messageExt.getBody());
        cetcMessage.setTags(messageExt.getTags());
        cetcMessage.setTopicName(messageExt.getTopic());
        cetcMessage.setCurrentRetryConsumeCount(messageExt.getReconsumeTimes());
        return cetcMessage;
    }
}
