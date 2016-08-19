package com.jayden.jxlx.rocketmq.producer;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.*;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.jayden.jxlx.constant.CommonConst;
import com.jayden.jxlx.rocketmq.cheker.Checker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User : jianjun.xu
 * Date : 2016/8/16
 * Time : 14:13
 * Desc :
 */
public abstract class AbstractProducer {
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractProducer.class);

	protected TransactionMQProducer producer;
	protected String nameServer;
	/**
	 * 做同样事情的Producer归为同一个Group，应用必须设置，并保证命名唯一
	 */
	private static final String PRODUCER_GROUP = "APP_PRODUCER_GROUP";
	public static List<Checker> checkerList = new ArrayList<Checker>();
	static {
	}
	public void init() {
		if (StringUtils.isBlank(System.getProperty(CommonConst.ROCKETMQ_NAMESRV_DOMAIN))) {
			System.setProperty(CommonConst.ROCKETMQ_NAMESRV_DOMAIN, nameServer);
		}
		TransactionCheckListener transactionCheckListener = new TransactionCheckListener() {
			@Override
			public LocalTransactionState checkLocalTransactionState(MessageExt messageExt) {
				Object msg = JSON.toJSONBytes(messageExt.getBody());
				return getCommitStatus(messageExt.getTopic(), messageExt.getTags(), msg);
			}
		};
		producer = new TransactionMQProducer(PRODUCER_GROUP);
		//事务回查最小并发数
		producer.setCheckThreadPoolMinSize(2);
		//事务回查最大并发数
		producer.setCheckThreadPoolMaxSize(2);
		//队列数
		producer.setCheckRequestHoldMax(2000);
		producer.setNamesrvAddr(nameServer);
		producer.setInstanceName(getInstanceName());
		//设置消息体最大值，默认只有128k
		producer.setMaxMessageSize(1024 * 512);
		//设置事务会查监听器
		producer.setTransactionCheckListener(transactionCheckListener);
		try {
			producer.start();
		} catch (MQClientException e) {
			LOGGER.error("producer start error! group:{},ex:{}", PRODUCER_GROUP, e.getErrorMessage());
		}

		LOGGER.info("metaq start success! nameServer={},producer group:{}", nameServer, PRODUCER_GROUP);
	}

	private LocalTransactionState getCommitStatus(String topic, String tags, Object msg) {
		for (Checker checker :checkerList) {
			if (checker.in(topic, tags)) {
				return checker.check(msg);
			}
		}
		return LocalTransactionState.COMMIT_MESSAGE;
	}

	public void destroy() {
		if (producer != null) {
			producer.shutdown();
			LOGGER.info("metaq sender close success! rocketmq.namesrv.domain:{}", nameServer);
		}
	}

	public abstract String getTopic();

	public abstract String getTags();
	public String getInstanceName() {
		return "APP_AbstractProducer" + "_" + getTopic() + "_" + getTags();
	}

	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}

	/**
	 * 发送消息服务
	 *
	 * @param message 消息对象
	 * @param topic   topic名称
	 * @param tag     tag名称
	 * @return 发送结果
	 */
	public abstract SendResult sendMessage(Serializable message, String topic, String tag);

	/**
	 * 发送事务消息
	 *
	 * @param message
	 * @param topic
	 * @param tag
	 * @param transactionExecuter
	 * @return
	 */
	public abstract TransactionSendResult sendMessage(Serializable message, String topic, String tag, LocalTransactionExecuter transactionExecuter);
}
