package com.cetc36.chameleon.mq;

import com.cetc36.chameleon.mq.api.TopicListener;
import com.cetc36.chameleon.mq.model.MessageStatus;
import com.cetc36.chameleon.mq.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestTopicListenerTestBean implements TopicListener {

    @Override
    public String getSubscriberBeanName() {
        return "subscriberService1";
    }

    @Override
    public String getTopicName() {
        return "PAY_ORDER";
    }

    @Override
    public String getTagExpression() {
        return "*";
    }

    @Override
    public MessageStatus subscribe(TopicMessage topicMessage) {
        log.info("TopicMessage: {}", topicMessage);
        return MessageStatus.ConsumeFail;
        // return MessageStatus.ConsumeSuccess;
    }
}
