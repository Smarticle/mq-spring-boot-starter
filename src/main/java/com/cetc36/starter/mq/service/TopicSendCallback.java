package com.cetc36.starter.mq.service;

import com.cetc36.starter.mq.model.TopicMessageSendResult;
import com.cetc36.starter.mq.exception.TopicMQException;

/**
 * 异步发送完成后, 回调接口.
 */
public interface TopicSendCallback {


    /**
     * 发送成功回调的方法.
     *
     * @param topicMessageSendResult 发送结果
     */
    void onSuccess(TopicMessageSendResult topicMessageSendResult);

    /**
     * 发送失败回调方法.
     *
     * @param topicMqException 异常信息.
     */
    void onFail(TopicMQException topicMqException);

}
