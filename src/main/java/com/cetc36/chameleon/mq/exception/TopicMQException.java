package com.cetc36.chameleon.mq.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息发送失败统一异常类
 */
@Getter
@Setter
public class TopicMQException extends MQException {
    private static final long serialVersionUID = 1L;

    private String topicName;

    /**
     * 消息 的 tag 来源于发送方配置值
     */
    private String tag;

    /**
     * 业务唯一id，由发送方发送时传递的值.
     */
    private String bizId;


    /**
     * 默认异常构造函数.
     */
    public TopicMQException() {
    }

    /**
     * 异常接口构造函数
     *
     * @param message 需要向外传递的异常信息
     */
    public TopicMQException(String message) {
        super(message);
    }

    /**
     * 异常接口构造函数
     *
     * @param message   需要向外传递的异常信息
     * @param topicName topicName
     */
    public TopicMQException(String message, String topicName) {
        super(message);
        this.topicName = topicName;
    }

    /**
     * 异常接口构造函数
     *
     * @param cause 需要向外传递的异常
     */
    public TopicMQException(Throwable cause) {
        super(cause);
    }

    /**
     * 异常接口构造函数
     *
     * @param message 需要向外传递的异常信息
     * @param cause   需要向外传递的异常
     */
    public TopicMQException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 异常接口构造函数
     *
     * @param message   需要向外传递的异常信息
     * @param topicName topicName
     * @param cause     需要向外传递的异常
     */
    public TopicMQException(String message, String topicName, Throwable cause) {
        super(message, cause);
        this.topicName = topicName;
    }


}

