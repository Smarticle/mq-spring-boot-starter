package com.cetc36.chameleon.mq.service.impl.ocean;

import com.cetc36.chameleon.mq.api.TopicPoller;
import com.cetc36.chameleon.mq.model.TopicMessage;
import com.cetc36.chameleon.mq.util.ApacheRocketMQUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

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
public class ApacheRocketMQPoller implements TopicPoller {

    /**
     * apache rocketmq pull consumer
     */
    DefaultLitePullConsumer poller;

    String beanName;

    boolean isStarted;

    public ApacheRocketMQPoller(DefaultLitePullConsumer poller, String beanName) {
        this.poller = poller;
        this.beanName = beanName;
    }

    public ApacheRocketMQPoller() {
    }


    @Override
    public List<TopicMessage> poll(long timeout) {
        // 拉取消息
        List<MessageExt> messages = poller.poll(timeout);
        // 转换成TopicMessage
        return ApacheRocketMQUtil.convertToCETCMessageList(messages);
    }

    @Override
    public void commit() {
        poller.commit();
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
        log.info("【MQ】ApacheRocketMQPuller[" + beanName + "] start...");
        try {
            poller.start();
            isStarted = true;
        } catch (MQClientException e) {
            log.error("【MQ】ApacheRocketMQPuller[" + beanName + "] start error", e);
        }
    }

    @Override
    public void close() {
        log.info("【MQ】ApacheRocketMQPuller[" + beanName + "] close...");
        poller.shutdown();
    }

}
