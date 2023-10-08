package com.guzt.starter.mq.pojo;

/**
 * 消费消息的返回结果
 *
 * @author liuyang
 */
public enum MessageStatus {
    /**
     * 消费成功，继续消费下一条消息
     */
    CommitMessage,
    /**
     * 消费失败，告知服务器稍后再投递这条消息，继续消费其他消息
     */
    ReconsumeLater,
}
