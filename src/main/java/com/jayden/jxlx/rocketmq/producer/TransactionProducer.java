package com.jayden.jxlx.rocketmq.producer;


import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.producer.LocalTransactionExecuter;
import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.TransactionSendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.jayden.jxlx.enums.TopicEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * User : jianjun.xu
 * Date : 2016/8/19
 * Time : 17:00
 * Desc :
 */
public class TransactionProducer extends AbstractProducer {
	private final static Logger LOGGER = LoggerFactory.getLogger(TransactionProducer.class);

	@Override
	public String getTopic() {
		return TopicEnum.TEMP_TOPIC.getTopic();
	}

	@Override
	public String getTags() {
		return TopicEnum.TEMP_TOPIC.getTags();
	}


	@Override
	public SendResult sendMessage(Serializable message, String topic, String tag) {
		SendResult sendResult = null;
		Message msg = new Message(topic, tag, JSON.toJSONBytes(message));
		try {
			sendResult = producer.send(msg);
			LOGGER.info("send msg success! msg:{}, result:{}", JSON.toJSONString(msg), JSON.toJSONString(sendResult));
		} catch (Exception e) {
			LOGGER.error("send msg error.msg:{}, e:{}", JSON.toJSONString(msg), e.getMessage());
			e.printStackTrace();
		}
		return sendResult;
	}

	@Override
	public TransactionSendResult sendMessage(Serializable message, String topic, String tag, LocalTransactionExecuter transactionExecuter) {
		TransactionSendResult sendResult = null;
		if(transactionExecuter == null){
			transactionExecuter = new TransactionExecuterImpl();
		}
		Message msg = new Message(topic, tag, JSON.toJSONBytes(message));
		try {
			//发送事务性消息
			sendResult = producer.sendMessageInTransaction(msg, transactionExecuter, null);
			if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
				LOGGER.error("send transaction msg failed! topic:{}, tags:{}, message:{}, sendResult:{}", topic, tag, msg, JSON.toJSONString(sendResult));
				return sendResult;
			}
		} catch (Exception e) {
			LOGGER.error("send transaction msg Exception! topic:{}, tags:{}, message:{}, sendResult:{}", topic, tag, msg, JSON.toJSONString(sendResult), e);
			return sendResult;
		}
		return sendResult;
	}
}
