package com.cetc36.starter.mq;

import com.cetc36.starter.mq.model.TopicMessage;
import com.cetc36.starter.mq.model.TopicMessageSendResult;
import com.cetc36.starter.mq.service.TopicPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestTopicPublisher {

    @Lazy
    @Resource
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

}
