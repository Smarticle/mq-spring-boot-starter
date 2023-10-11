package com.cetc36.chameleon.mq;

import com.cetc36.chameleon.mq.api.TopicSendCallback;
import com.cetc36.chameleon.mq.model.TopicMessage;
import com.cetc36.chameleon.mq.model.TopicMessageSendResult;
import com.cetc36.chameleon.mq.api.TopicPublisher;
import com.cetc36.chameleon.mq.exception.TopicMQException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class TestTopicPublisher {

    @Lazy
    @Resource(name="publishService1")
    private TopicPublisher topicPublisher1;

    @Test
    public void testPublish(){
        assertTrue(topicPublisher1.isStarted());
        assertFalse(topicPublisher1.isClosed());
        // 构建消息体
        TopicMessage msg1 = new TopicMessage();
        msg1.setTopicName("PAY_ORDER");
        // 模拟创建订单消息
        msg1.setTags("CREATE");
        msg1.setBizId(System.currentTimeMillis() + "");
        msg1.setMessageBody("testPublish".getBytes(StandardCharsets.UTF_8));
        TopicMessageSendResult result = topicPublisher1.publish(msg1);
        assertNotNull(result);
    }

    @Test
    public void testPublishAsync() throws InterruptedException {
        assertTrue(topicPublisher1.isStarted());
        assertFalse(topicPublisher1.isClosed());
        // 构建消息体
        TopicMessage msg1 = new TopicMessage();
        msg1.setTopicName("PAY_ORDER");
        // 模拟创建订单消息
        msg1.setTags("CREATE");
        msg1.setBizId(System.currentTimeMillis() + "");
        msg1.setMessageBody("testPublish".getBytes(StandardCharsets.UTF_8));
        topicPublisher1.publishAsync(msg1, new TopicSendCallback() {
            @Override
            public void onSuccess(TopicMessageSendResult topicMessageSendResult) {
                log.info("Ocean send success, topicMessageSendResult: {}", topicMessageSendResult.toString());
            }

            @Override
            public void onFail(TopicMQException topicMqException) {
                log.error("Ocean send fail", topicMqException);
            }
        });
    }

}
