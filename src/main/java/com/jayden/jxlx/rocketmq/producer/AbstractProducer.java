package com.jayden.jxlx.rocketmq.producer;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

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

	private static final String DOMAIN = "rocketmq.namesrv.domain";
	/**
	 * 做同样事情的Producer归为同一个Group，应用必须设置，并保证命名唯一
	 */
	private static final String PRODUCER_GROUP = "please_rename_unique_group_name";
	public void init() {
		if (StringUtils.isBlank(System.getProperty(DOMAIN))) {
			System.setProperty(DOMAIN, nameServer);
		}
		TransactionCheckListener transactionCheckListener = new TransactionCheckListenerImpl();
		producer = new TransactionMQProducer(PRODUCER_GROUP);
		producer.setNamesrvAddr(nameServer);
		//事务回查最小并发数
		producer.setCheckThreadPoolMinSize(2);
		//事务回查最大并发数
		producer.setCheckThreadPoolMaxSize(2);
		//队列数
		producer.setCheckRequestHoldMax(2000);
		producer.setInstanceName(getInstanceName());
		//设置消息体最大值
		producer.setMaxMessageSize(1024 * 1024 * 2);
		//设置事务会查监听器
		producer.setTransactionCheckListener(transactionCheckListener);
//		// 目前这种写法Rocket默认开启了VIP通道，VIP通道端口为10911-2=10909。若Rocket服务器未启动端口10909，则报connect to <> failed。
//		producer.setVipChannelEnabled(false);
		try {
			producer.start();
		} catch (MQClientException e) {
			LOGGER.error("producer start error! nameServer:{}, group:{}, ex:{}", nameServer, PRODUCER_GROUP, e.getErrorMessage());
		}
		LOGGER.info("producer start success! nameServer:{}, group:{}", nameServer, PRODUCER_GROUP);
	}
	public void destroy() {
		if (producer != null) {
			producer.shutdown();
			LOGGER.info("producer close success! nameServer:{}", nameServer);
		}
	}

	public abstract String getTopic();

	public abstract String getTags();

	public String getInstanceName() {
		return "Producer" + "_" + getTopic() + "_" + getTags();
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
