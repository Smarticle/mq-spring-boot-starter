package com.guzt.starter.mq.properties;

/**
 * 基础OSS配置文件
 */
public class BaseProperties {

    /**
     * 是否启用 true 启用  false 禁用
     */
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
