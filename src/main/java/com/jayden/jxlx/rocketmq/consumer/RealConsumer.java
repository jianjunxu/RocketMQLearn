package com.jayden.jxlx.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.jayden.jxlx.enums.TopicEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

/**
 * User : jianjun.xu
 * Date : 2016/8/16
 * Time : 14:55
 * Desc :
 */
public class RealConsumer extends AbstractConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RealConsumer.class);

	@Override
	public String getTopic() {
		return TopicEnum.TEMP_TOPIC.getTopic();
	}

	@Override
	public String getTags() {
		return TopicEnum.TEMP_TOPIC.getTags();
	}

	@Override
	public boolean doConsumeMessage(Serializable message) {
		LOGGER.info("UUID:" + UUID.randomUUID() + ",topic:" + getTopic() + ",msg:" + JSON.toJSONString(message));
		boolean result = true;
		LOGGER.info("accept msg success! msg:{}", message);
		return result;
	}
}
