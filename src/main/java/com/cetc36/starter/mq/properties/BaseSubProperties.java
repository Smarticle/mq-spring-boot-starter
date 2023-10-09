package com.cetc36.starter.mq.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * 基础消费者配置文件
 */
@Getter
@Setter
public class BaseSubProperties {

    /**
     * 最大失败重试消费次数 默认10次
     */
    private Integer maxRetryCount = 10;

}
