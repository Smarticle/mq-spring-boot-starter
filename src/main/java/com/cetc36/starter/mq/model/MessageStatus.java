package com.cetc36.starter.mq.model;

/**
 * 消费消息的返回结果
 *
 * @author liuyang
 */
public enum MessageStatus {
    /**
     * 消费成功，继续消费下一条消息
     */
    ConsumeSuccess,
    /**
     * 消费失败，告知服务器稍后再投递这条消息，继续消费其他消息
     */
    ConsumeFail,
}
