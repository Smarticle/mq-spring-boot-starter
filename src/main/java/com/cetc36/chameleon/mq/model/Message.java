package com.cetc36.chameleon.mq.model;

import java.io.Serializable;

/**
 * 公共抽象消息体
 *
 * @author liuyang
 */
public interface Message extends Serializable {

    /**
     * 获得消息体ID
     */
    String getMessageId();


    /**
     * 设置消息体ID
     */
    void setMessageId(String messageId);

    /**
     * 获得消息体
     */
    byte[] getMessageBody();

    /**
     * 设置消息体
     */
    void setMessageBody(byte[] messageBody);


}

