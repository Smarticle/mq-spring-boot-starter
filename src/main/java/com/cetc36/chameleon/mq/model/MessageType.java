package com.cetc36.chameleon.mq.model;

/**
 * 消费消息的返回结果
 *
 * @author liuyang
 */
public enum MessageType {

    /**
     * 普通消息
     */
    SIMPLE("SIMPLE"),
    ;

    private String value;

    public String getValue(){
        return this.value;
    }

    MessageType(String value) {
        this.value = value;
    }
}
