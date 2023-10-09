package com.cetc36.starter.mq.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息发送失败统一异常类
 *
 * @author liuyang
 */
@Getter
@Setter
public class MQException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String messageId;

    /**
     * 默认异常构造函数.
     */
    public MQException() {
    }

    /**
     * 异常接口构造函数
     *
     * @param message 需要向外传递的异常信息
     */
    public MQException(String message) {
        super(message);
    }

    /**
     * 异常接口构造函数
     *
     * @param cause 需要向外传递的异常
     */
    public MQException(Throwable cause) {
        super(cause);
    }

    /**
     * 异常接口构造函数
     *
     * @param message 需要向外传递的异常信息
     * @param cause 需要向外传递的异常
     */
    public MQException(String message, Throwable cause) {
        super(message, cause);
    }
}

