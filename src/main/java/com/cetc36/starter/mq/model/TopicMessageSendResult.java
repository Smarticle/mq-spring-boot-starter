package com.cetc36.starter.mq.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 公共Topic消息体 Message 发送结果
 *
 * @author liuyang
 */
@Getter
@Setter
@ToString
public class TopicMessageSendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一主键 由具体mq产品生成
     */
    private String messageId;

    /**
     * 业务唯一id，由发送方发送时传递的值.
     * 由发送方发送时传递的值
     */
    private String bizId;

    /**
     * 消息主题名称, 最长不超过255个字符; 由a-z, A-Z, 0-9, 以及中划线"-"和下划线"_"构成.
     * 一条合法消息本成员变量不能为空
     */
    private String topicName;

    /**
     * 消息 的 tag 来源于发送方配置值
     */
    private String tags;

}
