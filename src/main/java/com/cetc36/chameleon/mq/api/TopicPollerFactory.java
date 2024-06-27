package com.cetc36.chameleon.mq.api;

/**
 * 拉取消息到MQ服务端
 * 拉去（pull）模式
 *
 * @author liuyang
 */
public interface TopicPollerFactory {


    /**
     * 创建TopicPoller
     */
    TopicPoller create(String groupId, String topicName, String tagExpression, int pullBatchSize);
}

