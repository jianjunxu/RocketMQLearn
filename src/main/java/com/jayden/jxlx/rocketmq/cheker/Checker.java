package com.jayden.jxlx.rocketmq.cheker;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;

/**
 * User: 门海浩
 * Date: 14-5-8
 * Time: 下午2:01
 * To change this template use File | Settings | File Templates.
 */
public interface Checker {
    /**
     * 是否需要处理
     * @param topic
     * @param tag
     * @return
     */
    boolean in(String topic, String tag);

    /**
     * check事务消息处理好了没
     * @param msg
     * @return
     */
    LocalTransactionState check(Object msg);
}
