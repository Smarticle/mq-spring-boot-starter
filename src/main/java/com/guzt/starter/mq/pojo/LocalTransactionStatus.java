package com.guzt.starter.mq.pojo;

/**
 * 通知MQ服务器，本地事务执行结果.
 **/
public enum LocalTransactionStatus {

    /**
     * 已提交
     */
    COMMIT,

    /**
     * 回滚
     */
    ROLLBACK,

    /**
     * 未知，待MQ服务器再次检查
     */
    UNKNOW,
}
