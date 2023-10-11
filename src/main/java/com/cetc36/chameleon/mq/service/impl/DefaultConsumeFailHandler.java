package com.cetc36.chameleon.mq.service.impl;

import com.cetc36.chameleon.mq.api.ConsumeFailHandler;
import com.cetc36.chameleon.mq.model.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * MQ消费者尝试了最大次数后失败时的处理者
 */
@Slf4j
public class DefaultConsumeFailHandler implements ConsumeFailHandler {

    @Override
    public void handle(Message message) {
        log.debug("MQ消费者尝试了最大次数后失败时的处理方法， 你可以覆盖DefaultRetryConsumeFailHandler中的方法，RetryConsumeFailHandler： messageId={}", message.getMessageId());
    }
}
