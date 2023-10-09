package com.cetc36.starter.mq.model;

/**
 * 消息队列的发送场景
 *
 * @author liuyang
 */
public enum PublishType {

    /**
     * 首次发送
     */
    FIRST("FIRST"),

    /**
     * 发送失败重新发送
     */
    PUBLISH_FAIL_RETRY("PUBLISH_FAIL_RETRY"),

    /**
     * 消费失败重新发送
     */
    CONSUME_FAIL_RETRY("CONSUME_FAIL_RETRY");

    private String value;

    public String getValue() {
        return this.value;
    }

    PublishType(String value) {
        this.value = value;
    }
}
