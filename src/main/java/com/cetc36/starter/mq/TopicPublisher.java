package com.cetc36.starter.mq;

import com.cetc36.starter.mq.model.TopicMessage;
import com.cetc36.starter.mq.model.TopicMessageSendResult;
import com.cetc36.starter.mq.service.Manage;

/**
 * 推送消息到MQ服务端
 * 发布（pub）模式
 *
 * @author liuyang
 */
public interface TopicPublisher extends Manage {

    /**
     * 同步推送消息
     *
     * @param topicMessage 消息对象
     */
    TopicMessageSendResult publish(TopicMessage topicMessage);

    /**
     * 异步推送消息
     *
     * @param topicMessage      消息对象
     * @param topicSendCallback 异步结果处理
     */
    void publishAsync(TopicMessage topicMessage, TopicSendCallback topicSendCallback);

}

