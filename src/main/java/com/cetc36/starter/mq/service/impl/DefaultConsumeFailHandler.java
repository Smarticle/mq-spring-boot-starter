package com.cetc36.starter.mq.service.impl;

import com.cetc36.starter.mq.model.Message;
import com.cetc36.starter.mq.api.ConsumeFailHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * MQ消费者,尝试了最大次数后失败时的处理者
 */
@Slf4j
public class DefaultConsumeFailHandler implements ConsumeFailHandler {

    @Override
    public void handle(Message message) {
        log.debug("MQ消费者,尝试了最大次数后失败时的处理方法， 你可以覆盖DefaultRetryConsumeFailHandler中的方法，RetryConsumeFailHandler： messageId={}", message.getMessageId());
    }
}
