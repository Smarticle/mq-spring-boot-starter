package com.cetc36.chameleon.mq;

import com.cetc36.chameleon.mq.api.TopicPoller;
import com.cetc36.chameleon.mq.api.TopicPollerFactory;
import com.cetc36.chameleon.mq.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
public class TestTopicPoller {

//    @Resource(name = "pollerService1")
//    private TopicPoller topicPoller1;
//    @Resource(name = "pollerService2")
//    private TopicPoller topicPoller2;
    @Resource
    private TopicPollerFactory topicPollerFactory;

    @Test
    public void testPoller() {
//        assertTrue(topicPoller1.isStarted());
//        assertTrue(topicPoller2.isStarted());
//        assertFalse(topicPoller1.isClosed());
//        assertFalse(topicPoller2.isClosed());
//        List<TopicMessage> result1 = topicPoller1.poll(5000);
//        // topicPoller1.commit();
//        List<TopicMessage> result2 = topicPoller2.poll(5000);
//        // topicPuller2.commit();
//        assertNotNull(result1);
//        assertNotNull(result2);
        TopicPoller topicPoller = topicPollerFactory.create("group1", "PAY_ORDER1", "*", 1000);
        List<TopicMessage> result3 = topicPoller.poll();
        topicPoller.commit();
        assertNotNull(result3);
    }

}
