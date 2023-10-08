package com.guzt.starter.mq.properties;

/**
 * 基础消费者配置文件
 */
public class BaseSubProperties {

    /**
     * 最大失败重试消费次数 默认10次
     */
    private Integer maxRetryCount = 10;


    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }
}
