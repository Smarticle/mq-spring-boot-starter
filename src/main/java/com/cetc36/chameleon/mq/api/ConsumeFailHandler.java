package com.cetc36.chameleon.mq.api;


import com.cetc36.chameleon.mq.model.Message;

/**
 * MQ消费者尝试了最大次数后失败时的处理者
 *
 * @author liuyang
 */
public interface ConsumeFailHandler {

    /**
     * MQ消费者尝试了最大次数后失败时的处理方法
     *
     * @param message 消费失败的消息
     */
    void handle(Message message);
}
