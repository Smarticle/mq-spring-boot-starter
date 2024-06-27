package com.cetc36.chameleon.mq.api;


import com.cetc36.chameleon.mq.model.TopicMessage;
import com.cetc36.chameleon.mq.service.Manage;

import java.util.List;

/**
 * 推送消息到MQ服务端
 * 发布（pub）模式
 *
 * @author liuyang
 */
public interface TopicPoller extends Manage {


    /**
     * 订阅消息
     *
     * @param timeout 超时时间
     */
    List<TopicMessage> poll(long timeout);


    /**
     * 提交
     */
    void commit();


}

