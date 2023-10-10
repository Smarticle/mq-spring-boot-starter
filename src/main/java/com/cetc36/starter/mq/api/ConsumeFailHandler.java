package com.cetc36.starter.mq.api;


import com.cetc36.starter.mq.model.Message;

/**
 * MQ消费者,尝试了最大次数后失败时的处理者
 *
 * @author liuyang
 */
public interface ConsumeFailHandler {

    /**
     * 处理
     *
     * @param message 消费失败的消息
     */
    void handle(Message message);
}
