package com.cetc36.starter.mq.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Properties;

/**
 * 公共Topic消息体 Message
 *
 * @author liuyang
 */
@Getter
@Setter
@ToString
@SuppressWarnings("unused")
public class TopicMessage implements Message {
    private static final long serialVersionUID = 1L;

    /**
     * 用户其他属性
     */
    private Properties userProperties;

    /**
     * 消息唯一主键. 由具体mq产品生成
     */
    private String messageId;

    /**
     * 消息主题名称, 最长不超过255个字符; 由a-z, A-Z, 0-9, 以及中划线"-"和下划线"_"构成.
     * 一条合法消息本成员变量不能为空
     */
    private String topicName;

    /**
     * 消息标签, 合法标识符, 尽量简短且见名知意.
     * 建议传递该值
     */
    private String tags;

    /**
     * 业务主键，例如商户订单号.
     * 建议传递该值
     */
    private String bizId;

    /**
     * 消息体, 消息体长度默认不超过4M
     * 一条合法消息本成员变量不能为空
     */
    private byte[] messageBody;

    /**
     * 当前发送失败后，已经被重试发送的次数
     */
    private int currentRetryPublishCount;

    /**
     * 当前消费失败后，已经被重试消费的次数
     */
    private int currentRetryConsumeCount;

    /**
     * 默认构造函数; 必要属性后续通过Set方法设置.
     */
    public TopicMessage() {
        this(null, null, "", null);
    }

    /**
     * 有参构造函数.
     *
     * @param topicName    消息主题
     * @param tags         消息标签
     * @param bizId        业务主键
     * @param messageBody  消息体
     */
    public TopicMessage(String topicName, String tags, String bizId, byte[] messageBody) {
        this.topicName = topicName;
        this.tags = tags;
        this.bizId = bizId;
        this.messageBody = messageBody;
    }

    /**
     * 有参构造函数.
     *
     * @param messageId    唯一主键
     * @param topicName    消息主题
     * @param tags         消息标签
     * @param bizId        业务主键
     * @param messageBody  消息体
     */
    public TopicMessage(String messageId, String topicName, String tags, String bizId, byte[] messageBody) {
        this.messageId = messageId;
        this.topicName = topicName;
        this.tags = tags;
        this.bizId = bizId;
        this.messageBody = messageBody;
    }

    /**
     * 添加用户自定义属性键值对; 该键值对在消费消费时可被获取.
     *
     * @param key   自定义键
     * @param value 对应值
     */
    public void putUserProperties(final String key, final String value) {
        if (null == this.userProperties) {
            this.userProperties = new Properties();
        }

        if (key != null && value != null) {
            this.userProperties.put(key, value);
        }
    }

    /**
     * 获取用户自定义键的值
     *
     * @param key 自定义键
     * @return 用户自定义键值
     */
    public String getUserProperties(final String key) {
        if (null != this.userProperties) {
            return (String) this.userProperties.get(key);
        }
        return null;
    }
}

