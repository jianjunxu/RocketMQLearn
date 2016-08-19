package com.jayden.jxlx.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jayden.jxlx.constant.CommonConst;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * User : jianjun.xu
 * Date : 2016/8/16
 * Time : 14:13
 * Desc : mq消息消费基类
 */
public abstract class AbstractConsumer implements MessageListenerConcurrently {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

	protected DefaultMQPushConsumer consumer;
	protected String nameServer;
	protected int minConsumeThread = 2;
	protected int maxConsumeThread = 5;
	/**
	 * 做同样事情的Consumer归为同一个Group，应用必须设置，并保证命名唯一
	 */
	private static final String CONSUMER_GROUP = "APP_CONSUMER_GROUP";

	private final static int[] DELAY_LEVELS = new int[]{3, 5, 9, 14, 15, 16, 17, 18, 19, 20, 21};
	protected int maxRetryCount = 10;


	public void init() throws Exception {
		if (StringUtils.isBlank(System.getProperty(CommonConst.ROCKETMQ_NAMESRV_DOMAIN))) {
			System.setProperty(CommonConst.ROCKETMQ_NAMESRV_DOMAIN, nameServer);
		}
		consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
		consumer.setNamesrvAddr(nameServer);
		consumer.setConsumeThreadMin(minConsumeThread);
		consumer.setConsumeThreadMax(maxConsumeThread);
		consumer.setInstanceName(getInstanceName());
		consumer.subscribe(getTopic(), getTags());
		try {
			consumer.registerMessageListener(this);
			consumer.start();
		} catch (MQClientException e) {
			LOGGER.error("consumer start error! group:{},ex:{}", CONSUMER_GROUP, e.getErrorMessage());
		}
		LOGGER.info("consumer start! group:{}", CONSUMER_GROUP);
	}

	public void destroy() {
		if (consumer != null) {
			consumer.shutdown();
			LOGGER.info("consumer shutdown! group:{}", CONSUMER_GROUP);
		}
	}

	/**
	 * 基类实现消息监听接口，加上打印metaq监控日志的方法
	 */
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext context) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("receive_message:{}", msgList);
		if (msgList == null || msgList.size() < 1) {
			LOGGER.error("receive empty msg!");
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		int reConsumeTimes = msgList.get(0).getReconsumeTimes();
		if (reConsumeTimes >= maxRetryCount) {
			LOGGER.error("reConsumeTimes >" + maxRetryCount + "msgList:" + msgList + "context:" + context);
		}
		context.setDelayLevelWhenNextConsume(getDelayLevelWhenNextConsume(reConsumeTimes));
		boolean ret = true;
		for (MessageExt message : msgList) {
			if (!doConsumeMessage(decodeMsg(message))) {
				ret = false;
			}
		}
		ConsumeConcurrentlyStatus status = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		if (!ret) {
			status = ConsumeConcurrentlyStatus.RECONSUME_LATER;
		}
		LOGGER.info("ConsumeConcurrentlyStatus:{}|cost:{}", status, System.currentTimeMillis() - startTime);
		return status;
	}

	/**
	 * 根据重试次数设置重新消费延迟时间
	 *
	 * @param reConsumeTimes 重试的次数
	 * @return level级别
	 */
	public int getDelayLevelWhenNextConsume(int reConsumeTimes) {
		if (reConsumeTimes >= DELAY_LEVELS.length) {
			return DELAY_LEVELS[DELAY_LEVELS.length - 1];
		}
		return DELAY_LEVELS[reConsumeTimes];
	}

	private Serializable decodeMsg(MessageExt msg) {
		if (msg == null) {
			return null;
		}
		return JSON.toJSONString(msg.getBody());
	}


	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}

	public abstract String getTopic();

	public abstract String getTags();

	public abstract boolean doConsumeMessage(Serializable message);

	public String getInstanceName() {
		return "APP_AbstractConsumer" + "_" + getTopic() + "_" + getTags();
	}
}
