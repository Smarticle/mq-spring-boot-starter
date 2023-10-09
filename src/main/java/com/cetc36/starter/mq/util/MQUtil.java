package com.cetc36.starter.mq.util;

import com.cetc36.starter.mq.service.TopicSubscriber;
import com.cetc36.starter.mq.service.RetryConsumeFailHandler;
import com.cetc36.starter.mq.service.TopicListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * 工具类
 */
public class MQUtil {

    /**
     * 工具方法
     *
     * @param listenerMap TopicListener
     * @return Map
     */
    public static Map<String, List<TopicListener>> topicListenerGroupBySubBean(Map<String, TopicListener> listenerMap) {
        List<TopicListener> topicListenerList = new ArrayList<>(4);
        listenerMap.forEach((k, v) -> topicListenerList.add(v));
        //groupBy SubscriberBeanName
        return topicListenerList.stream().collect(groupingBy(TopicListener::getSubscriberBeanName));
    }

    /**
     * 工具方法 设置消费服务监听，并且启动消费服务
     *
     * @param subscriberBeanName       ignore
     * @param subscriber               ignore
     * @param listenerMapBySubBeanName ignore
     * @param retryConsumeFailHandler   ignore
     */
    public static void setListenerAndStartSub(
            String subscriberBeanName,
            TopicSubscriber subscriber,
            RetryConsumeFailHandler retryConsumeFailHandler,
            Map<String, List<TopicListener>> listenerMapBySubBeanName) throws IOException {

        List<TopicListener> listeners = listenerMapBySubBeanName.get(subscriberBeanName);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (TopicListener listener : listeners) {
            subscriber.subscribe(listener.getTopicName(), listener.getTagExpression(), listener);
        }
        subscriber.setRetryConsumeFailHandler(retryConsumeFailHandler);
        subscriber.start();
    }

}
