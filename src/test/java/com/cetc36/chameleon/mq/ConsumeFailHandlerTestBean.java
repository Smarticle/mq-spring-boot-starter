package com.cetc36.chameleon.mq;

import com.cetc36.chameleon.mq.api.ConsumeFailHandler;
import com.cetc36.chameleon.mq.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsumeFailHandlerTestBean implements ConsumeFailHandler {

    @Override
    public void handle(Message message) {
        log.info("消息失败次数过多不再重试。");
    }
}
